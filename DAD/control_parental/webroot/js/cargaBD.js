
var URL_BACKAPI = "http://127.0.0.1:8090/"

$.ajax({
    url: "/placasUsuario/1",
    cache: false,
    type : 'GET',
    success: function(data){
        for(i = 0; i<data.results.length; i++){
            console.log()
        }
        



    }
    
  });