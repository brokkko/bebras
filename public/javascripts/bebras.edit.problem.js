$(function() {
    $('form#edit-problem textarea').wymeditor({
        dialogImageUploadUrl:   '/wymupload',

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
        }
    });
});