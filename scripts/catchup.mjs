#!/usr/bin/env node

import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import { spawnSync } from 'node:child_process';

const defaultSourceRepoUrl = 'https://github.com/GeekTrainer/advanced-copilot-cli';

function usage() {
  console.log(`Usage: node catchup.mjs --asset-path <path> [--source-repo-url <url>] [--source-ref <ref>] [--source-path <path>] [--force] [target-repo]

Copies catch-up assets into an AssetTrack repository root.
Run from the AssetTrack repository root, or pass the repository path as target-repo.

Options:
  --source-repo-url <url>  Git repository URL to clone assets from.
                           Defaults to ${defaultSourceRepoUrl}.
  --source-ref <ref>       Branch or tag to clone. Defaults to main.
  --source-path <path>     Local course repository root or asset directory to copy from instead of Git.
  --asset-path <path>      Asset directory inside the source repository, such as assets/03.
  --force, -f              Overwrite existing catch-up files.
  --help, -h               Show this help.
`);
}

function fail(message) {
  console.error(message);
  process.exit(1);
}

function takeValue(args, index, flag) {
  if (index + 1 >= args.length || args[index + 1].startsWith('--')) {
    fail(`${flag} requires a value.`);
  }

  return args[index + 1];
}

function parseArgs(args) {
  const options = {
    sourceRepoUrl: defaultSourceRepoUrl,
    sourceRepoUrlProvided: false,
    sourceRef: 'main',
    sourcePath: '',
    assetPath: '',
    force: false,
    targetRepo: process.cwd(),
  };

  for (let index = 0; index < args.length; index += 1) {
    const arg = args[index];

    switch (arg) {
      case '--source-repo-url':
        options.sourceRepoUrl = takeValue(args, index, arg);
        options.sourceRepoUrlProvided = true;
        index += 1;
        break;
      case '--source-ref':
        options.sourceRef = takeValue(args, index, arg);
        index += 1;
        break;
      case '--source-path':
        options.sourcePath = takeValue(args, index, arg);
        index += 1;
        break;
      case '--asset-path':
        options.assetPath = takeValue(args, index, arg);
        index += 1;
        break;
      case '--force':
      case '-f':
        options.force = true;
        break;
      case '--help':
      case '-h':
        usage();
        process.exit(0);
        break;
      default:
        if (arg.startsWith('-')) {
          fail(`Unknown option: ${arg}`);
        }

        if (options.targetRepo !== process.cwd()) {
          fail(`Unexpected extra argument: ${arg}`);
        }

        options.targetRepo = arg;
        break;
    }
  }

  if (options.sourcePath && options.sourceRepoUrlProvided) {
    fail('Use either --source-path or --source-repo-url, not both.');
  }

  if (!options.assetPath) {
    fail('--asset-path is required.');
  }

  options.assetPath = normalizeAssetPath(options.assetPath);
  return options;
}

function normalizeAssetPath(assetPath) {
  const normalized = assetPath.replaceAll('\\', '/').replace(/^\/+|\/+$/g, '');
  const parts = normalized.split('/').filter(Boolean);

  if (parts.length === 0 || parts.includes('..')) {
    fail('--asset-path must be a relative path inside the source repository.');
  }

  return parts.join('/');
}

function ensureAssetTrackRoot(targetRoot) {
  if (!fs.existsSync(path.join(targetRoot, 'package.json')) || !fs.existsSync(path.join(targetRoot, 'services'))) {
    fail(`This does not look like the AssetTrack repository root: ${targetRoot}
Expected to find package.json and services/.`);
  }
}

function run(command, args, options = {}) {
  const result = spawnSync(command, args, {
    encoding: 'utf8',
    stdio: 'pipe',
    ...options,
  });

  if (result.error) {
    fail(`${command} failed: ${result.error.message}`);
  }

  if (result.status !== 0) {
    const output = `${result.stderr || ''}${result.stdout || ''}`.trim();
    fail(output || `${command} ${args.join(' ')} failed.`);
  }
}

function resolveLocalAssetRoot(sourcePath, assetPath) {
  const sourceRoot = path.resolve(sourcePath);
  const repoAssetRoot = path.join(sourceRoot, ...assetPath.split('/'));

  if (fs.existsSync(repoAssetRoot) && fs.statSync(repoAssetRoot).isDirectory()) {
    return { assetRoot: repoAssetRoot, sourceLabel: sourceRoot };
  }

  if (pathEndsWith(sourceRoot, assetPath)) {
    return { assetRoot: sourceRoot, sourceLabel: sourceRoot };
  }

  fail(`Could not find ${assetPath} in the local source path: ${sourceRoot}`);
}

function pathEndsWith(filePath, relativePath) {
  const fileParts = path.resolve(filePath).split(path.sep).filter(Boolean);
  const relativeParts = relativePath.split('/').filter(Boolean);

  if (relativeParts.length > fileParts.length) {
    return false;
  }

  return relativeParts.every((part, index) => part === fileParts[fileParts.length - relativeParts.length + index]);
}

function cloneAssetSource(sourceRepoUrl, sourceRef, assetPath, tempRoot) {
  const cloneRoot = path.join(tempRoot, 'source-repo');

  run('git', [
    'clone',
    '--quiet',
    '--depth',
    '1',
    '--branch',
    sourceRef,
    '--filter=blob:none',
    '--sparse',
    sourceRepoUrl,
    cloneRoot,
  ]);

  run('git', ['-C', cloneRoot, 'sparse-checkout', 'set', '--cone', assetPath]);

  const assetRoot = path.join(cloneRoot, ...assetPath.split('/'));
  if (!fs.existsSync(assetRoot) || !fs.statSync(assetRoot).isDirectory()) {
    fail(`Could not find ${assetPath} in the source repository.`);
  }

  return {
    assetRoot,
    sourceLabel: sourceRepoUrl.replace(/\/$/, '').replace(/\.git$/, ''),
  };
}

function collectFiles(root) {
  const files = [];

  function walk(currentDir, relativeDir = '') {
    for (const entry of fs.readdirSync(currentDir, { withFileTypes: true })) {
      if (entry.isDirectory() && entry.name === 'scripts') {
        continue;
      }

      const absolutePath = path.join(currentDir, entry.name);
      const relativePath = path.join(relativeDir, entry.name);

      if (entry.isDirectory()) {
        walk(absolutePath, relativePath);
      } else if (entry.isFile()) {
        files.push(relativePath);
      }
    }
  }

  walk(root);
  return files.sort();
}

function copyFiles(assetRoot, targetRoot, sourceFiles, force) {
  const existing = sourceFiles.filter((relativePath) => fs.existsSync(path.join(targetRoot, relativePath)));

  if (!force && existing.length > 0) {
    fail(`Refusing to overwrite existing files. Re-run with --force to overwrite:
${existing.map((file) => `  ${file}`).join('\n')}`);
  }

  for (const relativePath of sourceFiles) {
    const source = path.join(assetRoot, relativePath);
    const destination = path.join(targetRoot, relativePath);
    fs.mkdirSync(path.dirname(destination), { recursive: true });
    fs.copyFileSync(source, destination);
  }
}

function main() {
  const options = parseArgs(process.argv.slice(2));
  const targetRoot = path.resolve(options.targetRepo);
  ensureAssetTrackRoot(targetRoot);

  const tempRoot = fs.mkdtempSync(path.join(os.tmpdir(), 'catchup-'));

  try {
    const source = options.sourcePath
      ? resolveLocalAssetRoot(options.sourcePath, options.assetPath)
      : cloneAssetSource(options.sourceRepoUrl, options.sourceRef, options.assetPath, tempRoot);

    const sourceFiles = collectFiles(source.assetRoot);
    if (sourceFiles.length === 0) {
      fail(`No files found in ${options.assetPath}.`);
    }

    copyFiles(source.assetRoot, targetRoot, sourceFiles, options.force);

    console.log(`Catch-up assets copied from ${source.sourceLabel} ${options.assetPath} to ${targetRoot}, excluding scripts/ directories.`);
    console.log('');
    console.log('Next verification steps from the AssetTrack repository root:');
    console.log('  1. Run /instructions and confirm repo and scoped instructions are loaded.');
    console.log('  2. Run /agent and confirm accessibility-updater is available.');
    console.log('  3. Run /skills and confirm make-repo-contribution is available.');
  } finally {
    fs.rmSync(tempRoot, { recursive: true, force: true });
  }
}

main();