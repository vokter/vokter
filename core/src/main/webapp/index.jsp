<%@ page import="argus.Context" %>
<!DOCTYPE html>
<% Context context = Context.getInstance(); %>
<% boolean isStoppingEnabled = context.isStoppingEnabled(); %>
<% boolean isStemmingEnabled = context.isStemmingEnabled(); %>
<% boolean ignoreCase = context.isIgnoringCase(); %>
<html lang="en" xmlns="http://www.w3.org/1999/html">
<head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <meta charset="utf-8">
    <title>Argus</title>
    <meta name="generator" content="Bootply" />
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">

    <link href="css/bootstrap.min.css" rel="stylesheet">
    <link href="css/font-awesome.css" rel="stylesheet">

    <script type='text/javascript' src="js/jquery-1.11.1.min.js"></script>
    <script type='text/javascript' src="js/bootstrap.min.js"></script>
    <script type='text/javascript'>
        $(document).ready(function() {

            $("#searchform").submit(function(event) {
                event.preventDefault();

                $('#results').html('<div style="text-align: center; margin-top: 50px; "><span class="glyphicon glyphicon-refresh glyphicon-refresh-animate"></span></div>');

                var documentUrl = $("#documentUrlField").val();
                var keywords = $("#keywordsField").val();
                var interval = $("#intervalField").val();
                var responseUrl = $("#responseUrlField").val();

                $.ajax({
                    type: "POST",
                    url: "/web/search",
                    data: "{ \"documentUrl\": \""+documentUrl+"\", \"keywords\": ["+keywords+"] , \"interval\": "+interval+", \"responseUrl\": "+responseUrl+" }",
                    contentType: "application/json",
                    dataType: "html",
                    success: function (htmlResponse) {
                        $('#results').html(htmlResponse);

                        $(".result").slice(0, 10).show();
                        $("#load").click(function(e){
                            e.preventDefault();
                            $(".result:hidden").slice(0, 10).show();
                            if($(".result:hidden").length == 0){
                                $("#load").hide()
                            }
                        });
                    }
                });

            });

        });
    </script>

    <style type="text/css">

        .result {
            display:none;
        }

        .glyphicon-refresh-animate {
            -animation: spin .7s infinite linear;
            -webkit-animation: spin2 .7s infinite linear;
        }

        @-webkit-keyframes spin2 {
            from { -webkit-transform: rotate(0deg);}
            to { -webkit-transform: rotate(360deg);}
        }

        @keyframes spin {
            from { transform: scale(1) rotate(0deg);}
            to { transform: scale(1) rotate(360deg);}
        }
        .wrap {
            width:100%;
            height: 50px;
            margin:0 auto;
        }
        .full_col {
            margin-top: 30px;
            margin-bottom: 30px;
            list-style:none;
            clear:both;
        }
        .left_col {
            float:left;
            width:40%;
        }
        .right_col {
            float:right;
            width:40%;
        }

    </style>
</head>

<body>

<div class="container">

    <form action="" id="searchform" class="navbar-form" method="post">
        <div class="form-group" style="display:inline;">
            <div class="input-group">
                <input id="documentUrlField" type="text" autofocus class="form-control">
                <input id="keywordsField" type="text" autofocus class="form-control">
                <input id="intervalField" min="0" type="number" style="margin-top:8px;" class="form-control" value="0">
            </div>

            <input type="submit" value="Watch">
        </div>
    </form>

</div>

<div id="results">
</div>

</body>
</html>