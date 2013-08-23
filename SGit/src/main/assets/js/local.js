$(document).ready(function() {
    var rawCodes = CodeLoader.getCode();
    var highlighted = hljs.highlightAuto(rawCodes);
    var length = CodeLoader.getLineNumber();
    var lineNumbersList = []
    for (var i = 0; i < length; i++) {
        var lineNumber = i + 1;
        var line = lineNumber + '.';
      	lineNumbersList.push(line);
    }
    var lineNumbers = lineNumbersList.join('\n')
    $('.line_numbers').html(lineNumbers);
    $('.codes code').html(highlighted.value);
    $('.codes code').addClass(highlighted.language)
});