(
f = {|rawSec, gmtOffset = 2|
	var year = 1970;
	var month;
	var day = rawSec div: 86400;
	var monthDayCnt = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
	var date;

	while({day >= 0},
		{
			var yearDayCnt;
			var prestupnyRok = (mod((year - 1968),4) == 0).if( {true}, {false} );
			prestupnyRok.if(
				{monthDayCnt[1] = 29; yearDayCnt = 366;},
				{monthDayCnt[1] = 28; yearDayCnt = 365;}
			);

			(day < yearDayCnt).if({
				month = 0;
				while({day >= 0},
					{
						var hour, minute, sec;
						(day < monthDayCnt[month]).if({
							hour = (((rawSec/3600)%24)+gmtOffset).floor;
							minute = ((rawSec/60)%60).floor;
							sec = (rawSec%60).floor;

							date = [year, month+1, day+1, hour, minute, sec];
							"Date : %/%/% [%h %m %s]".format(date[0], date[1], date[2], date[3], date[4], date[5]).postln;
						});
						day = day - monthDayCnt[month];
						month = month + 1;
					}
				);
			});

			day = day - yearDayCnt;
			year = year + 1;
		}
	);
	date;
}
)
f.value(Date.getDate.rawSeconds);
f.value(File.mtime("C:/GitHub/supercollider/Histories/test.scd"));
f.value(File.mtime("C:/GitHub/supercollider/Histories/151005_joach_kof_remote.scd"));
f.value(File.mtime("C:/Dropbox/Galerie/1.jpg"))
f.value(File.mtime("C:/Dropbox/Galerie/grass test01.png"))
f.value(1297894762)
f.value(0000000003)