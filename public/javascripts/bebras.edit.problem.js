$(function() {
    $('form#edit-problem textarea.bebras-editor').wymeditor({
        dialogImageUploadUrl:   '/wymupload',

        classesItems: [
            {'name': 'center', 'title': 'IMG: Center image', 'expr': 'img'},
            {'name': 'float-left', 'title': 'IMG: Float left', 'expr': 'img'},
            {'name': 'float-right', 'title': 'IMG: Float right', 'expr': 'img'},
            {'name': 'clear', 'title': 'PARA: Clear both', 'expr': 'p'}
        ],

        styles: //TODO move this to original css
            '/* IMG: Center image */                                             '+
            'img.center {                                                        '+
            '  /* display:block; margin-left:auto;margin-right:auto; */          '+
            '}                                                                   '+
            '/* IMG: Float left */                                               '+
            'img.float-left {                                                    '+
            '  /* float:left; */                                                 '+
            '}                                                                   '+
            '/* IMG: Float right */                                              '+
            'img.float-right {                                                   '+
            '  /* float:right; */                                                '+
            '}                                                                   '+
            '/* PARA: Clear both */                                              '+
            'p.clear {                                                           '+
            '  /* clear:both; */                                                 '+
            '}                                                                   ',

        boxHtml:
            "<div class='wym_box'>"
            + "<div class='wym_area_top'>"
            + WYMeditor.TOOLS
            + "<div class='wym_left_block'>" + WYMeditor.CONTAINERS + "</div>"
            + "<div class='wym_right_block'>" + WYMeditor.CLASSES + "</div>"
            + "</div>"
            + "<div class='wym_area_left'></div>"
            + "<div class='wym_area_right'>"
            + "</div>"
            + "<div class='wym_area_main'>"
            + WYMeditor.HTML
            + WYMeditor.IFRAME
            + WYMeditor.STATUS
            + "</div>"
            + "<div class='wym_area_bottom'>"
            + "</div>"
            + "</div>",

        postInit: function(wym) {
            wym.image_upload();
            //make classes dropdown
            wym._box.find('.wym_classes').removeClass('wym_panel').addClass('wym_dropdown');

            $(wym._iframe).css('height', '400px');
        }
    });
});