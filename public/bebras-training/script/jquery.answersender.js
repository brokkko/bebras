(function($){
  $.fn.answerSender = function(){
    var task = this;
    var inputs = task.find('.answers form input[type=text]');
    inputs.each(function(){
      var el = $(this);
      el.attr("id", task.attr("id")+"_input");
      var send = $("<input type='submit' value='Отправить' />");
      send.attr("id", task.attr("id")+"_send");
      send.attr("onClick", "$('#"+task.attr("id")+"_send').attr('value', 'Изменить');" +
          " $('#"+task.attr("id")+"_clear').show();" +
          " alert('send:  "+task.attr("id")+"-'+$('#"+task.attr("id")+"_input').attr('value'));" +
          " return false;");
      el.after(send);

      var clear = $("<input type='submit' value='Отменить' />");
      clear.attr("id", task.attr("id")+"_clear");
      clear.attr("onClick", "$('#"+task.attr("id")+"_send').attr('value', 'Отправить');" +
          " $('#"+task.attr("id")+"_clear').hide(); " +
          " $('#"+task.attr("id")+"_input').attr('value', '');" +
          " alert('send:  "+task.attr("id")+"-empty');" +
          " return false;");
      clear.hide();
      send.after(clear);
    });

    var $radios = task.find('.answers input[type=radio]');
    $radios.each(function(){
      /* Было так:
      var el = $(this);
      el.attr("name", task.attr("id")+"_group");
      el.attr("onClick", "alert('send:  "+task.attr("id")+"-"+el.attr("value")+"');");*/
      //alert('here');
      // Здесь был Андрей
      var $el = $(this);
      var task_id = task.attr("id");
      $el.attr("name", task_id+"_group");
      $el.unbind("click");
      $el.click( function() {
          alert('click')
          var value = $el.attr("value");
          var task_id = task.attr("id");
          var value_num = 0;
          var date_send = 1; //time();
          
          /**/
          
            switch (value) {
            case 'ans1':
                value_num = 1;
                break;
            case 'ans2':
                value_num = 2;
                break;
            case 'ans3':
                value_num = 3;
                break;
            case 'ans4':
                value_num = 4;
                break;
            }
          
          var answer_key = 'answer_'+task_id;
          var date_key = 'date_send_'+task_id;
          var data_to_send = {/*
                answer_key: value_num,
                date_key: date_send
          */};
          data_to_send[answer_key] = value_num;
          data_to_send[date_key] = date_send;
          var tip = 'Посылаем на сервер: {\''+answer_key+'\': '+value_num+', \''+date_key+'\': '+date_send+'}';
          //alert(tip);
          
          jQuery.ajax({
            url: '/beaver/ajax/answer_send',
            dataType : "html", //xml",
            data: data_to_send,
            type : "POST",
            success: function (response, textStatus) {
                alert('И сервер ответил:' + response)
            }
        });
        
          // /beaver/ajax/answer_send
      });
    });

    return this;
  }
})(jQuery);