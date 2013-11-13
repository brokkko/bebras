$(function() {
    var $undo_edit_block_actions = $('.html-block-undo-action');

    $undo_edit_block_actions.hide();

    $undo_edit_block_actions.click(function() {
        document.location.reload(true);
        return false;
    });

    $('.html-block-edit-action').click(function() {
        var $this = $(this);
        var is_edit = $this.hasClass('editing');
        var $block = $this.parents('.html-block');

        //TODO use update selector: http://trac.wymeditor.org/trac/wiki/0.5/Customization.html

        if (is_edit) {
            //turn edit off
            $this.removeClass('editing');

            var $form = $block.find('form');
            var $wym = $block.data("editor");

            var html = $wym.xhtml();
            $wym.update();

            var $contents = $("<div></div>");
            $contents.addClass('contents');
            $contents.addClass('admin');
            $contents.html(html);

            $.ajax({
                type: "POST",
                url: $form.attr('action'),
                data: $form.serialize()
            });

            $form.remove();
//            $wym._box.remove();
            $block.append($contents);

            $block.find('.html-block-edit-action').text('Edit');
            $block.find('.html-block-undo-action').hide();
        } else {
            //turn edit on
            $this.addClass('editing');

            $contents = $block.find('.contents');

            html = $contents.html();

            $form = $('<form></form>');
            $form.attr('method', 'POST');
            $form.attr('action', $block.find('.html-block-ref').text());

            var $textarea = $('<textarea></textarea>');
            $textarea.attr('name', 'html');
            $textarea.addClass('pure-html');
            $textarea.text(html);

            $form.append($textarea);

            $contents.remove();
            $block.append($form);

            var editor = $textarea.wymeditor({
                dialogImageUploadUrl:   '/wymupload',
                postInit: function(wym) {
                    $block.data("editor", wym);
                    $(wym._iframe).css('height', '600px');
                    wym.image_upload();
                }
            });

            $block.find('.html-block-edit-action').text('Save');
            $block.find('.html-block-undo-action').show();
        }

        return false;
    });
});