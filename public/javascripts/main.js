/**
 * Created with IntelliJ IDEA.
 * User: work
 * Date: 18.04.13
 * Time: 17:33
 * To change this template use File | Settings | File Templates.
 */

$(function (){
    window.changeType = function (event) {
        if (typeof  window.changeType.allChk == 'undefined'){
            window.changeType.allChk = [];
        }
        $(window.changeType.allChk).each(function(index){
            $('#' + this).hide();
            $('#' + this + ' input').removeAttr('required');
        });
        $('#' + this.value).show();
        $('#' + this.value + ' input').attr('required', true);
    };
});