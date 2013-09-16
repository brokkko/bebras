/*jslint evil: true */
/**
    WYMeditor.image_upload
    ====================
	
    A plugin to add an upload field to the image selector.
	
	
	Todo:
		- 
	
	by Patabugen ( patabugen.co.uk )
*/

WYMeditor.editor.prototype.image_upload = function() {
    var wym = this;
	var uploadUrl = wym._options.dialogImageUploadUrl;
	// Check the options
	if (uploadUrl == undefined) {
		WYMeditor.console.warn(
			"You should define the WYMeditor option dialogImageUploadUrl for the image_upload."
		);
		// With no upload URL we cannot upload files
		return;
	}
	
	
	// Write some JS to Ajaxify the form and put the response where we want it.
	var script = String() +
		'<script type="text/javascript" src="' + wym._options.basePath + 'plugins/image_upload/jquery.iframe-post-form.js"></script>' +
		'<script type="text/javascript">' +
			// We do this on window load rather than DOM Ready to be sure it's run after the WYMEditor
			// things are run. Else the unbind won't work.
			'jQuery(window).load(function(){' +
				'var oldSubmitLabel = jQuery("form#image_upload_form .submit").val();' +
				// WYMEditor automatically locks onto any form here, so remove the binding.
				'jQuery("form#image_upload_form").unbind("submit");' +
				'jQuery("form#image_upload_form").iframePostForm({' +
					'iframeID: "image_upload_iframe", ' +
					'json: "true", ' +
					'post: function(response){ ' +
						'jQuery("form#image_upload_form .submit").val(jQuery("#image_upload_uploading_label").val() + "..."); ' +
					'}, ' +
					'complete: function(response){ ' +
						'response = response[0]; ' +
						'jQuery(".wym_src").val(response.thumbUrl); ' +
						'jQuery(".wym_alt").val(response.original_filename); ' +
						'jQuery("form#image_upload_form .submit").val(oldSubmitLabel); ' + 
					'} ' +
				'})' +
			' });' +
		'</script>';
		
	// Put together the whole dialog script
	wym._options.dialogImageHtml = String() +
            '<body class="wym_dialog wym_dialog_image" ' +
                    'onload="WYMeditor.INIT_DIALOG(' + WYMeditor.INDEX + ')">' +
				// We have to put this in a new form, so we don't break the old one
                '<form id="image_upload_form" method="post" enctype="multipart/form-data" action="' + uploadUrl + '">' +
                    '<fieldset>' +
                        '<legend>{Upload} {Image}</legend>' +
                        '<div class="row">' +
                            '<label>{Upload}</label>' +
                            '<input type="file" name="uploadedfile" />' +
                        '</div>' +
                        '<div class="row">' +
                            '<label>{Size}</label>' +
                            '<select id="image_upload_size" name="thumbnailSize">' +
								'<option value="actual">{Actual}</option>' +
								'<option value="small">{Small}</option>' +
								'<option value="medium">{Medium}</option>' +
								'<option value="large">{Large}</option>' +
                        '</div>' +
                        '<div class="row row-indent">' +
							// We use a hidden value here so we can get a proper translation
							'<input type="hidden" id="image_upload_uploading_label" value="{Uploading}" />' +
                            '<input type="submit" class="submit" ' +
                                'value="{Upload}" />' +
                        '</div>' +
                     '</fieldset>' +
                '</form>' +
                '<form>' +
                    '<fieldset>' +
                        '<input type="hidden" class="wym_dialog_type" ' +
                            'value="' + WYMeditor.DIALOG_IMAGE + '" />' +
                        '<legend>{Image}</legend>' +
                        '<div class="row">' +
                            '<label>{URL}</label>' +
                            '<input type="text" class="wym_src" value="" ' +
                                'size="40" autofocus="autofocus" />' +
                        '</div>' +
                        '<div class="row">' +
                            '<label>{Alternative_Text}</label>' +
                            '<input type="text" class="wym_alt" value="" size="40" />' +
                        '</div>' +
                        '<div class="row">' +
                            '<label>{Title}</label>' +
                            '<input type="text" class="wym_title" value="" size="40" />' +
                        '</div>' +
                        '<div class="row row-indent">' +
                            '<input class="wym_submit" type="submit" ' +
                                'value="{Submit}" />' +
                            '<input class="wym_cancel" type="button" ' +
                                'value="{Cancel}" />' +
                        '</div>' +
                    '</fieldset>' +
                '</form>' +
				script +
            '</body>';
};
