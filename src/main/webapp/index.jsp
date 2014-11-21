<%@ page import="argus.rest.Context" %>
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

                var queryField = $("#queryfield").val();
                var slopField = $("#slopField").val();

                $.ajax({
                    type: "POST",
                    url: "/web/search",
                    data: "{ \"query\": \""+queryField+"\", \"slop\": "+slopField+" }",
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

<nav class="navbar navbar-inverse navbar-fixed-top" role="navigation">
    <div class="container-fluid">

        <!-- Collect the nav links, forms, and other content for toggling -->
        <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
            <ul class="nav navbar-nav navbar-right">
                <li><input id="slopField" min="0" type="number" style="margin-top:8px;" class="form-control" value="0"></li>
                <li><a href="#" data-toggle="modal" data-target="#search-modal"><i class="icon-cog icon-large"></i></a></li>
            </ul>

            <form action="" id="searchform" class="navbar-form" method="post">
                <div class="form-group" style="display:inline;">
                    <div class="input-group">
                        <input id="queryfield" type="text" autofocus class="form-control" placeholder="Search">
                        <span class="input-group-addon"><span class="glyphicon glyphicon-search"></span></span>
                    </div>
                </div>
            </form>

        </div>
    </div>
</nav>

<div class="container">

    <br/><br/>

    <!-- Search settings -->
    <div class="modal container fade" id="search-modal" data-width="1000" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h2 class="form-signin-heading">Search Settings</h2>

                    <div class="modal-header">
                        <form action="/web/save-settings/" method="POST" enctype="multipart/form-data">
                            <div class="wrap">
                                <div class="full_col">
                                    <div class="left_col">
                                        <% if (isStoppingEnabled) { %>
                                        <input type="checkbox"
                                               id="isStoppingEnabled"
                                               name="isStoppingEnabled"
                                               checked />
                                        <% } else { %>
                                        <input type="checkbox"
                                               id="isStoppingEnabled"
                                               name="isStoppingEnabled" />
                                        <% } %>
                                        Filter stopwords<br/>
                                        <% if (isStemmingEnabled) { %>
                                        <input type="checkbox"
                                               id="isStemmingEnabled"
                                               name="isStemmingEnabled"
                                               checked />
                                        <% } else { %>
                                        <input type="checkbox"
                                               id="isStemmingEnabled"
                                               name="isStemmingEnabled" />
                                        <% } %>
                                        Use porter stemmer<br/>
                                        <% if (ignoreCase) { %>
                                        <input type="checkbox"
                                               id="ignoreCase"
                                               name="ignoreCase"
                                               checked />
                                        <% } else { %>
                                        <input type="checkbox"
                                               id="ignoreCase"
                                               name="ignoreCase" />
                                        <% } %>
                                        Ignore case
                                    </div>
                                    <div class="right_col">
                                        <br>
                                        <input type="submit" class="btn btn-default form-control" name="press" value="Save" />
                                    </div>
                                </div>
                            </div>
                        </form>
                    </div>

                    <!-- Upload stopwords from file -->
                    <form action="/web/upload-stop/" method="POST" enctype="multipart/form-data">
                        <div class="wrap">
                            <div class="full_col">
                                <div class="left_col">
                                    <input id="stopwordFile" name="stopwordFile" type="file" value=""/>
                                </div>
                                <div class="right_col">
                                    <input type="submit" class="form-control" name="press" value="Upload stopwords" />
                                </div>
                            </div>
                        </div>
                    </form>

                    <!--<div class="modal-footer">-->
                    <!--<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>-->
                    <!--</div>-->
                </div>
            </div>
        </div>
    </div>

</div>

<div id="results">
</div>

</body>
</html>