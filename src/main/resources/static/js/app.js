$(function () {
    $('#asset-filter-form select').on('change', function () {
        $('#asset-filter-form').submit();
    });

    $('form[action="/assignments/return"]').on('submit', function () {
        return window.confirm('Mark this asset as returned?');
    });
});
