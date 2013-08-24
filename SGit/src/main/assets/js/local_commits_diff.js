$(document).ready(function() {

    function colorDiff(diff) {
        if (diff !== undefined) {
            var highlighted = hljs.highlight("diff", diff, true);
            var codeBlock = $('<code>').html(highlighted.value);
            codeBlock = $('<pre>', {class:"diff"}).append(codeBlock);
            codeBlock = $('<div>', {class:"diff-block"}).append(codeBlock);
            $('body').append(codeBlock);
        }
    }

    window.notifyEntriesReady = function() {
        var length = CodeLoader.getDiffSize();
        for (var i = 0; i < length; i++) {
            var diff = CodeLoader.getDiff(i);
            colorDiff(diff);
        }
    };

    CodeLoader.getDiffEntries();

});