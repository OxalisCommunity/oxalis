<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8"/>
        <title>Oxalis statistics download</title>
        <link rel="stylesheet" href="http://code.jquery.com/ui/1.10.2/themes/smoothness/jquery-ui.css"/>
        <script src="http://code.jquery.com/jquery-1.9.1.js"></script>
        <script src="http://code.jquery.com/ui/1.10.2/jquery-ui.js"></script>
        <link rel="stylesheet" href="css/oxalis.css"/>
        <script>
            $(function () {
                $("#startDate").datepicker();
                $("#endDate").datepicker();

                $.datepicker.setDefaults({
                    dateFormat: 'yy-mm-dd',
                    changeMonth: true,
                    changeYear: true,
                    minDate: new Date(2013,1-1,1),    // No statistics available before this date
                    maxDate: new Date(),
                    firstDay: 1,
                    numberOfMonths: 1,
                    monthNames: ['Januar','Februar','Mars','April','Mai','Juni','Juli','August','September','Oktober','November','Desember'],
                    monthNamesShort: ['Jan','Feb','Mar','Apr','Mai','Jun','Jul','Aug','Sep','Okt','Nov','Des'],
                    weekHeader: 'Uke',
                    dayNamesShort: ['S&oslash;n','Man','Tir','Ons','Tor','Fre','L&oslash;r'],
                    dayNames: ['S&oslash;ndag','Mandag','Tirsdag','Onsdag','Torsdag','Fredag','L&oslash;rdag'],
                    dayNamesMin: ['S&oslash;','Ma','Ti','On','To','Fr','L&oslash;'],
                    nextText: 'Neste m&aring;ned',
                    prevText: 'Forrige m&aring;ned',
                    showOn: 'button',
                    buttonImage: '/images/calendar.gif',
                    buttonImageOnly: true,
                    buttonText: 'Velg dato...',
                    yearRange: '2013:c+10',    // Go no further back than 2013, allow for current year +10
                    showWeek: true,
                    numberOfMonths: 1   // Number of months to display

                });

                // Set default date for start
                $("#startDate").val('2013-01-01');

                // Set default date for end date
                $("#endDate").val($.datepicker.formatDate('yy-mm-dd', new Date()));

                // Displays a receipt of the download
                $("form").submit(function() {
                    return true;
                });
            });
        </script>
    </head>

    <body>
        <h1>Oxalis statistics</h1>



        <div id="picture">
            <img id="oxalis" src="images/oxalis.jpg" />
        </div>

        <p>Download PEPPOL statistics from the period given below.</p>
        <div id="form1">
            <form method="GET" action="resource/messagefact">
                <p>Start: <input type="text" id="startDate" name="start"/></p>

                <p>End: <input type="text" id="endDate" name="end"/></p>

                <p>Granularity <select name="granularity">
                    <option value="Y">Annual</option>
                    <option value="M">Monthly</option>
                    <option value="D">Daily</option>
                    <option value="H" selected>Hourly</option>
                </select>
                </p>
                <input type="submit" value="Download in CSV format"/>
            </form>
        </div>

    </body>
</html>

