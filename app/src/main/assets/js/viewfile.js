(function() {
  var displayFileContent, rawCodes, lang;

  displayFileContent = function() {
    var highlighted, i, length, lineNumbers, lineNumbersList;
    rawCodes || (rawCodes = CodeLoader.getCode());
    if (lang && lang != "null") {
      highlighted = hljs.highlight(lang, rawCodes, true);
      $('.codes code').html(highlighted.value);
      $('.codes code').addClass(highlighted.language);
    } else {
      $('.codes code').html(rawCodes);
    }
    length = rawCodes.replace(/\r\n/g, '\n').replace(/\r/g, '\n').split('\n').length;
    lineNumbersList = (function() {
      var _i, _results;
      _results = [];
      for (i = _i = 1; 1 <= length ? _i <= length : _i >= length; i = 1 <= length ? ++_i : --_i) {
        _results.push(i + '.');
      }
      return _results;
    })();
    lineNumbers = lineNumbersList.join('\n');
    return $('.line_numbers').html(lineNumbers);
  };

  window.display = displayFileContent;

  window.setLang = function(_lang) {
    lang = _lang;
    displayFileContent();
  };

  $(document).ready(function() {
    return CodeLoader.loadCode();
  });

}).call(this);
