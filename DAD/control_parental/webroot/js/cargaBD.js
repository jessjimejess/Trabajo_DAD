//Peticiones AJAX al cargar la página
var idPlacaFORM;

$.ajax({
    url: "/placasUsuario/1",
    cache: false,
    type : 'GET',
    success: function(data){
        var on = "on"
        var off = "off"
        for(i = 0; i<data.results.length; i++){
            console.log(data)
           
            
            
            var statusdisp = statusToString(data.results[i]["3"])
            var statuscp = statusToString(data.results[i]["4"])
            
            var h4 = $("<h4><i class = 'fas fa-microchip'></i>" + data.results[i]["1"].toUpperCase() + "</h4>")
            var p1 = $("<p> Dispositivo: " + statusdisp + "</p>")
            var p2 = $("<p> Control parental: " + statuscp + "</p>")
            
            if(statuscp == "APAGADO"){
                var buttoncp = $("<button class = 'button activar' id = '" + data.results[i]["0"] + "' onclick = 'return accionMQTT(" + data.results[i]["0"] + "," + 1 + ");'>ACTIVAR</button>")
            }else{
                var buttoncp = $("<button class = 'button desactivar' id = '" + data.results[i]["0"] + "' onclick = 'return accionMQTT(" + data.results[i]["0"] + "," + 0 + ");'>DESACTIVAR</button>")
            }
            
            var buttonhist = $("<button class = 'button historial' id = '" + data.results[i]["0"] + "' onclick = 'return showHistorial(" + data.results[i]["0"] + ");'>HISTORIAL</button>")
            var buttonhistcp = $("<button class = 'button historial_cp' id = '" + data.results[i]["0"] + "' onclick = 'return showHistorialcp(" + data.results[i]["0"] + ");'>HISTORIAL CP</button>")
            
            var div = $("<div></div>").addClass("placa").append(h4,p1,p2,buttoncp,buttonhist,buttonhistcp)
            
            $(".placas_body").append(div)
        }
        



    }
    
  });


  function showHistorialcp(idPlaca){
    $.ajax({
        url: "/controlParentalPlaca/" + idPlaca,
        cache: false,
        type: 'GET',
        success: function(data){
            
            var table = document.getElementById('tabla_historial').getElementsByTagName('tbody')[0];
            $(".columnas_hist").remove();

            for(i = 0; i<data.results.length; i++){

                fechaInicioUTC = data.results[i]["2"]
                fechaFinUTC = data.results[i]["3"]

                var arrayFecha = tratarFechas(fechaInicioUTC, fechaFinUTC)

                //Creación dinámica de la tabla de historiales
                var row = table.insertRow()
                row.setAttribute("class", "columnas_hist")
                cell0 = row.insertCell(0).append(arrayFecha[0])
                cell1 = row.insertCell(1).append(arrayFecha[1])
                cell2 = row.insertCell(2).append(arrayFecha[2])

               
            
            }
            $("#page-mask").fadeIn(300);
            $(".historial_placa").dialog({width: '50%',
                                          close: CloseFunction})
                                          .fadeIn(300);
            

        }

    });
}




function showHistorial(idPlaca){
    $.ajax({
        url: "/historialPlaca/" + idPlaca,
        cache: false,
        type: 'GET',
        success: function(data){
            
            var table = document.getElementById('tabla_historial').getElementsByTagName('tbody')[0];
            $(".columnas_hist").remove();

            for(i = 0; i<data.results.length; i++){

                fechaInicioUTC = data.results[i]["2"]
                fechaFinUTC = data.results[i]["3"]

                var arrayFecha = tratarFechas(fechaInicioUTC, fechaFinUTC)

                //Creación dinámica de la tabla de historiales
                var row = table.insertRow()
                row.setAttribute("class", "columnas_hist")
                cell0 = row.insertCell(0).append(arrayFecha[0])
                cell1 = row.insertCell(1).append(arrayFecha[1])
                cell2 = row.insertCell(2).append(arrayFecha[2])

               
            
            }
            $("#page-mask").fadeIn(300);
            $(".historial_placa").dialog({width: '50%',
                                          close: CloseFunction})
                                          .fadeIn(300);
            

        }

    });
}


function accionMQTT(idPlaca, accion){
    console.log(idPlaca + " " + accion)
    if(accion){
        console.log("Enviando peticion MQTT para activa CP")
        idPlacaFORM = idPlaca;
        $("#page-mask").fadeIn(300);
        $(".form_on").dialog({close: CloseFunction,
                              modal:true,
                              }).fadeIn(300)
        //A partir de aquí todo queda en mano del formulario y su listener



    }else{
        console.log("Enviando peticion MQTT para desactivar CP")
        accion = "off"
        mqttApiRequest(idPlaca, accion, 0);
    }

}


function submitForm(){
    console.log(idPlacaFORM)
    var fechaFin = $("#in_fechaFin").val()
    var d = new Date(fechaFin).getTime()/1000;
    console.log(d)
    
    //Llamamos a la función para hacer la petición ON con AJAX.
    mqttApiRequest(idPlacaFORM, "on", d)
}

//A partir de aquí las funciones son de utilities
function statusToString(data){
    if(data == 0){
        return "ENCENDIDO"

    }else{
        return "APAGADO"
    }


}


//Calculo de la diferencia entre las 2 fechas y devolución como array de Strings para inserción en HTML
//Lo que hace esta función es una fumada
function tratarFechas(fechaInicioUTC, fechaFinUTC){
    
    fechaInicio = moment(new Date(parseInt(fechaInicioUTC * 1000)))
    fechaFin = moment(new Date(parseInt(fechaFinUTC * 1000)))
    
    var stringDiferencia = String(fechaFin.diff(fechaInicio, 'days')) + " dias, " +  
                           String(fechaFin.diff(fechaInicio, 'hours')) + " horas, " + 
                           String(fechaFin.diff(fechaInicio, 'minutes')) + " min. "

    var listSplitIn = String(fechaInicio).split(" ")
    var listSplitFin = String(fechaFin).split(" ")

    var stringInicio = listSplitIn[1] + " " + listSplitIn[2] + " " + listSplitIn[3] + " " + listSplitIn[4]
    var stringFin = listSplitFin[1] + " " + listSplitFin[2] + " " + listSplitFin[3] + " " + listSplitFin[4]
    
    return [stringInicio, stringFin, stringDiferencia]

    
}

//Solicitud de desconexión MQTT
function mqttApiRequest(idPlaca, accion, fechaFin){
    console.log(idPlaca)
    $.ajax({
        url: "/mqtt",
        dataType: 'json',
        cache: false,
        type: 'POST',
        processData: false,
        contentType: 'application/json',
        data: JSON.stringify({
            "idPlaca" : idPlaca,
            "action" : accion,
            "fechaFin" : fechaFin,
        
        }),

        
        success: function(data){
            if(accion == "off"){
                alert("Control parental desactivado")
                location.reload(true); //No caché
            }


            if(accion = "on"){
                $(".form_on").dialog("close")
                alert("Control parental activado")
                location.reload(true);
            }

            
        },
        statusCode: {
            500: function() {
            alert('Server error');

            

            }
        },
        statusCode: {
            400: function() {
            alert('Error 400, compruebe:\n -Que la fecha esté bien introducida\n -Que la placa esté correctamente conectada al servidor MQTT');

            

            }
        }

    });




}

function CloseFunction(){
    $("#page-mask").fadeOut(300);


}