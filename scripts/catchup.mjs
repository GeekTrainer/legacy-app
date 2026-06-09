#!/usr/bin/env node

import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import { spawnSync } from 'node:child_process';

const defaultAssetsRepoUrl = 'https://github.com/GeekTrainer/advanced-copilot-cli';

function usage() {
  console.log(`Usage: node catchup.mjs --assets-path <path> [--assets-repo-url <url>] [--assets-repo-branch-name <name>] [--assets-local-path <path>] [--force]

Copies catch-up assets into an AssetTrack repository root.
Run from the AssetTrack repository root.

Options:
  --assets-repo-url <url>  Git repository URL to clone assets from.
                           Defaults to ${defaultAssetsRepoUrl}.
  --assets-repo-branch-name <name>
                           Source repository branch to clone. Defaults to main.
  --assets-local-path <path>
                           Local course repository root or asset directory to copy from instead of Git.
  --assets-path <path>     Asset directory inside the source repository, such as assets/03.
  --force, -f              Overwrite existing catch-up files.
  --help, -h               Show this help.
`);
}

function fail(message) {
  throw new Error(message);
}

function takeValue(args, index, flag) {
  if (index + 1 >= args.length || args[index + 1].startsWith('--')) {
    fail(`${flag} requires a value.`);
  }

  return args[index + 1];
}

function parseArgs(args) {
  const options = {
    assetsRepoUrl: defaultAssetsRepoUrl,
    assetsRepoUrlProvided: false,
    assetsRepoBranchName: 'main',
    assetsRepoBranchNameProvided: false,
    assetsLocalPath: '',
    assetsPath: '',
    force: false,
  };

  for (let index = 0; index < args.length; index += 1) {
    const arg = args[index];

    switch (arg) {
      case '--assets-repo-url':
        options.assetsRepoUrl = takeValue(args, index, arg);
        options.assetsRepoUrlProvided = true;
        index += 1;
        break;
      case '--assets-repo-branch-name':
        options.assetsRepoBranchName = takeValue(args, index, arg);
        options.assetsRepoBranchNameProvided = true;
        index += 1;
        break;
      case '--assets-local-path':
        options.assetsLocalPath = takeValue(args, index, arg);
        index += 1;
        break;
      case '--assets-path':
        options.assetsPath = takeValue(args, index, arg);
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

        fail(`Unexpected argument: ${arg}. Run this script from the AssetTrack repository root.`);
    }
  }

  if (options.assetsLocalPath && options.assetsRepoUrlProvided) {
    fail('Use either --assets-local-path or --assets-repo-url, not both.');
  }

  if (options.assetsLocalPath && options.assetsRepoBranchNameProvided) {
    fail('Use --assets-repo-branch-name only when cloning assets from --assets-repo-url.');
  }

  if (!options.assetsPath) {
    fail('--assets-path is required.');
  }

  options.assetsPath = normalizeAssetsPath(options.assetsPath);
  return options;
}

function normalizeAssetsPath(assetsPath) {
  const normalized = assetsPath.replaceAll('\\', '/').replace(/^\/+|\/+$/g, '');
  const parts = normalized.split('/').filter(Boolean);

  if (parts.length === 0 || parts.includes('..')) {
    fail('--assets-path must be a relative path inside the source repository.');
  }

  return parts.join('/');
}

function ensureAssetTrackRoot(targetRoot) {
  if (!fs.existsSync(path.join(targetRoot, 'package.json')) || !fs.existsSync(path.join(targetRoot, 'services'))) {
    fail(`This does not look like the AssetTrack repository root: ${targetRoot}
Expected to find package.json and services/.`);
  }
}

function run(command, args) {
  const result = spawnSync(command, args, {
    encoding: 'utf8',
    stdio: 'pipe',
  });

  if (result.error) {
    fail(`${command} failed: ${result.error.message}`);
  }

  if (result.status !== 0) {
    const output = `${result.stderr || ''}${result.stdout || ''}`.trim();
    fail(output || `${command} ${args.join(' ')} failed.`);
  }
}

function resolveLocalAssetRoot(assetsLocalPath, assetsPath) {
  const sourceRoot = path.resolve(assetsLocalPath);
  const repoAssetRoot = path.join(sourceRoot, ...assetsPath.split('/'));

  if (fs.existsSync(repoAssetRoot) && fs.statSync(repoAssetRoot).isDirectory()) {
    return { assetRoot: repoAssetRoot, sourceLabel: sourceRoot };
  }

  if (pathEndsWith(sourceRoot, assetsPath)) {
    return { assetRoot: sourceRoot, sourceLabel: sourceRoot };
  }

  fail(`Could not find ${assetsPath} in the local source path: ${sourceRoot}`);
}

function pathEndsWith(filePath, relativePath) {
  const fileParts = path.resolve(filePath).split(path.sep).filter(Boolean);
  const relativeParts = relativePath.split('/').filter(Boolean);

  if (relativeParts.length > fileParts.length) {
    return false;
  }

  return relativeParts.every((part, index) => part === fileParts[fileParts.length - relativeParts.length + index]);
}

function cloneAssetSource(assetsRepoUrl, assetsRepoBranchName, assetsPath, tempRoot) {
  const cloneRoot = path.join(tempRoot, 'source-repo');

  run('git', [
    'clone',
    '--quiet',
    '--depth',
    '1',
    '--branch',
    assetsRepoBranchName,
    '--filter=blob:none',
    '--sparse',
    assetsRepoUrl,
    cloneRoot,
  ]);

  run('git', ['-C', cloneRoot, 'sparse-checkout', 'set', '--cone', assetsPath]);

  const assetRoot = path.join(cloneRoot, ...assetsPath.split('/'));
  if (!fs.existsSync(assetRoot) || !fs.statSync(assetRoot).isDirectory()) {
    fail(`Could not find ${assetsPath} in the source repository.`);
  }

  return {
    assetRoot,
    sourceLabel: assetsRepoUrl.replace(/\/$/, '').replace(/\.git$/, ''),
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
  const targetRealRoot = fs.realpathSync(targetRoot);
  const existing = sourceFiles.filter((relativePath) => fs.existsSync(path.join(targetRoot, relativePath)));

  if (!force && existing.length > 0) {
    fail(`Refusing to overwrite existing files. Re-run with --force to overwrite:
${existing.map((file) => `  ${file}`).join('\n')}`);
  }

  for (const relativePath of sourceFiles) {
    const source = path.join(assetRoot, relativePath);
    const destination = path.join(targetRoot, relativePath);
    ensureSafeDestination(targetRoot, targetRealRoot, destination);
    fs.copyFileSync(source, destination);
  }
}

function ensureSafeDestination(targetRoot, targetRealRoot, destination) {
  const destinationParent = path.dirname(destination);
  rejectSymlinkComponents(targetRoot, destinationParent);
  fs.mkdirSync(destinationParent, { recursive: true });
  rejectSymlink(destination);
  ensureInsideRoot(targetRealRoot, fs.realpathSync(destinationParent));
}

function rejectSymlinkComponents(targetRoot, filePath) {
  const relativePath = path.relative(targetRoot, filePath);
  if (relativePath === '') {
    rejectSymlink(targetRoot);
    return;
  }

  let current = targetRoot;
  for (const part of relativePath.split(path.sep)) {
    current = path.join(current, part);
    rejectSymlink(current);
  }
}

function rejectSymlink(filePath) {
  if (fs.existsSync(filePath) && fs.lstatSync(filePath).isSymbolicLink()) {
    fail(`Refusing to write through symlink: ${filePath}`);
  }
}

function ensureInsideRoot(targetRealRoot, filePath) {
  const relative = path.relative(targetRealRoot, filePath);
  if (relative === '' || (!relative.startsWith('..') && !path.isAbsolute(relative))) {
    return;
  }

  fail(`Refusing to write outside target repository: ${filePath}`);
}

function main() {
  const options = parseArgs(process.argv.slice(2));
  const targetRoot = process.cwd();
  ensureAssetTrackRoot(targetRoot);

  let tempRoot;

  try {
    let source;
    if (options.assetsLocalPath) {
      source = resolveLocalAssetRoot(options.assetsLocalPath, options.assetsPath);
    } else {
      tempRoot = fs.mkdtempSync(path.join(os.tmpdir(), 'catchup-'));
      source = cloneAssetSource(options.assetsRepoUrl, options.assetsRepoBranchName, options.assetsPath, tempRoot);
    }

    const sourceFiles = collectFiles(source.assetRoot);
    if (sourceFiles.length === 0) {
      fail(`No files found in ${options.assetsPath}.`);
    }

    copyFiles(source.assetRoot, targetRoot, sourceFiles, options.force);

    console.log(`Catch-up assets copied from ${source.sourceLabel} ${options.assetsPath} to ${targetRoot}, excluding scripts/ directories.`);
    console.log('');
    console.log('Next verification steps from the AssetTrack repository root:');
    console.log('  1. Run /instructions and confirm repo and scoped instructions are loaded.');
    console.log('  2. Run /agent and confirm accessibility-updater is available.');
    console.log('  3. Run /skills and confirm make-repo-contribution is available.');
  } finally {
    if (tempRoot) {
      fs.rmSync(tempRoot, { recursive: true, force: true });
    }
  }
}

try {
  main();
} catch (error) {
  console.error(error instanceof Error ? error.message : String(error));
  process.exitCode = 1;
}