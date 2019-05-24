//Peticiones AJAX al cargar la página
var URL_BACKAPI = "http://127.0.0.1:8090/"

$.ajax({
    url: "/placasUsuario/1",
    cache: false,
    type : 'GET',
    success: function(data){
        for(i = 0; i<data.results.length; i++){
            console.log(data)
           
            
            var statusdisp = statusToString(data.results[i]["3"])
            var statuscp = statusToString(data.results[i]["4"])
            
            var h4 = $("<h4><i class = 'fas fa-microchip'></i>" + data.results[i]["1"].toUpperCase() + "</h4>")
            var p1 = $("<p> Dispositivo: " + statusdisp + "</p>")
            var p2 = $("<p> Control parental: " + statuscp + "</p>")
            
            if(statuscp == "APAGADO"){
                var buttoncp = $("<button class = 'button activar' id = '" + data.results[i]["0"] + "'>ACTIVAR CP</button>")
            }else{
                var buttoncp = $("<button class = 'button desactivar' id = '" + data.results[i]["0"] + "'>DESACTIVAR CP</button>")
            }
            
            var div = $("<div></div>").addClass("placa").append(h4,p1,p2, buttoncp)
            
            
            $(".placas_body").append(div)
        }
        



    }
    
  });

//Utilities
function statusToString(data){
    if(data == 0){
        return "APAGADO"

    }else{
        return "ENCENDIDO"
    }


}