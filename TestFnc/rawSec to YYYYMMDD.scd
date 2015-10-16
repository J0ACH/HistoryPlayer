(
f = {|rawSec|
	// var rawSec = time.rawSeconds;
	var year, month, day, hour, minute, sec;
	var restDay;
	"\nTargetAnswer %".format(Date.getDate).postln;
	"rawSec %".format(rawSec).postln;
	year = ((rawSec/31556926)+1970);
	"\nYYYY :  %".format(year.floor).postln;
	month = (((rawSec/2629743)%12));
	"MM :  %".format((month+1).floor).postln;
	//ok


	// "month %".format(month).postln;
	restDay = mod(month,1);
	// restDay =rawSec - (( year/31556926)+1970);
	// "restDay %".format(restDay).postln;

	// day = (restDay * 30.44)+2;
	day = ((restDay*30.44)+1).ceil;
	"DD :  %  -- prob with day".format(day.floor).postln;


	//ok
	hour = (((rawSec/3600)%24)+2).floor;
	"HH :  %".format(hour).postln;
	minute = ((rawSec/60)%60).floor;
	"MIN :  %".format(minute).postln;
	sec = (rawSec%60).floor;
	"sec :  %".format(sec).postln;
	nil
}
)
f.value(Date.getDate.rawSeconds);
f.value(File.mtime("C:/GitHub/supercollider/Histories/test.scd"));
f.value(File.mtime("C:/GitHub/supercollider/Histories/151005_joach_kof_remote.scd"));
f.value(File.mtime("C:/Dropbox/Galerie/1.jpg"))
f.value(File.mtime("C:/Dropbox/Galerie/grass test01.png"))
f.value(1297894762)
f.value(0000000003)


(
var start;
start = Main.elapsedTime;
{ loop({(Main.elapsedTime - start).asTimeString.postln; 0.05.wait}) }.fork;
)
Main.elapsedTime;
Date.getDate.rawSeconds.asTimeString.postln;


mod(4.25,4).postln
