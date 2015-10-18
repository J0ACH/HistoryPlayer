HP_track{
	classvar version = 0.04;

	var parent;
	var tracks;

	var view, objects;
	var origin;
	var sizeX, sizeY;
	var displayMode;



	var >template;

	var historyFile, historyLines;
	var isLoad;


	var mouseClickStartX, mouseClickStartY, mouseClickButton;
	var isMouseOver;

	*new{ |path|
		^super.new.init(path);
	}

	init{ |path|
		historyFile = PathName(path);
		historyLines = History.loadCS(path).lines.reverse;

		isLoad = true;
		isMouseOver = false;

		objects = Dictionary.new;


		this.setDisplayMode(\smallTrack);
		// this.print(\lines);
		this.print(\duration);
		this.print(\modify);
		// this.modifyDate.postln;
	}

	setDisplayMode {|mode|
		displayMode = mode;
		switch (mode,
			\smallTrack, {
				sizeX = 300;
				sizeY = 22;
			},
			\infoTrack, {
				sizeX = 300;
				sizeY = 50;
			},
		);
	}

	historyPath { ^historyFile.fullPath; }

	name { ^historyFile.fileNameWithoutExtension; }

	duration {
		isLoad.if(
			{ ^historyLines[historyLines.size-1][0].asFloat; },
			{ ^nil; }
		);
	}

	players {
		var player = Set.new;
		historyLines.do({|oneLine|
			player.add(oneLine[1].asSymbol);
		});
		^player.asArray;
	}

	modifyEpochTime { ^File.mtime(this.historyPath); }

	modifyDate {
		var	date = this.formatDate(this.modifyEpochTime);
		^("%/%/%".format(date[2], date[1], date[0])).asString;
	}

	initGUI{|parent, inPoint, dimX, dimY|
		origin = inPoint;
		sizeX = dimX;
		sizeY = dimY;

		view = UserView.new(parent, Rect(origin.x, origin.y, sizeX, sizeY))
		.background_(template[\colorBackground])
		.acceptsMouseOver_(true)
		.resize_(2);

		view.drawFunc = {|uview|

			uview.moveTo(origin.x,origin.y);


			isMouseOver.if(
				{Pen.strokeColor = template[\colorFront];},
				{Pen.strokeColor = template[\colorActive];}
			);

			Pen.addRect(Rect(0,0, uview.bounds.width, uview.bounds.height));
			Pen.stroke;
			// uview.moveTo(originX, originY);

			// Pen.moveTo(0@uview.bounds.height.rand);
			// Pen.lineTo(uview.bounds.width@uview.bounds.height.rand);
			// Pen.stroke;
		};

		view.mouseDownAction = {|w, x, y, modKey, buttNum, clickCnt| this.mouseDownFunc(w, x, y, buttNum); };
		// view.mouseUpAction = {|me, x, y, mod| this.mouseUpFunc(me, x, y, mod);  };
		view.mouseMoveAction = {|w, x, y, modKey| this.mouseMoveFunc(w, x, y); };
		view.mouseOverAction = {|w, x, y| this.mouseOverAction(w, x, y); };
		view.mouseEnterAction = {|w, x, y|
			isMouseOver = true;
			this.setDisplayMode(\infoTrack);
			w.resizeTo(sizeX,sizeY);
			w.refresh;
		};
		view.mouseLeaveAction = {|w, x, y|
			isMouseOver = false;
			this.setDisplayMode(\smallTrack);
			w.resizeTo(sizeX,sizeY);
			w.refresh;
		};



		objects.put(\name, StaticText( view, Rect.fromPoints((5@5), (150@30)))
			.string_(this.name;)
			.font_(template[\fontChapter])
			.stringColor_(template[\colorFront])
			.align_(\topLeft)
		);

		objects.put(\duration, StaticText( view, Rect.fromPoints((150@5), ((view.bounds.width-5)@30)))
			.string_(this.formatTime(this.duration))
			.font_(template[\fontChapter])
			.stringColor_(template[\colorFront])
			.align_(\topRight)
		);

		objects.put(\date, StaticText( view, Rect.fromPoints((50@30), ((view.bounds.width-5)@50)))
			.string_(this.modifyDate)
			.font_(template[\fontChapter])
			.stringColor_(template[\colorFront])
			.align_(\topRight)
		);

		objects.put(\players, StaticText( view, Rect.fromPoints((5@30), ((view.bounds.width-70)@50)))
			.string_(this.players)
			.font_(template[\fontChapter])
			.stringColor_(template[\colorFront])
			.align_(\topRight)
		);



		// parent.refresh;
		^view;
	}

	mouseDownFunc {|w, x, y, buttNum|
		mouseClickButton = buttNum;
		mouseClickStartX = x;
		mouseClickStartY = y;
	}

	mouseUpFunc {|w, x, y, mod|
		postf("end path: (startX-x)==XXX mouse coordinates:[%,%]\n", x,y); //(startX-x),

	}

	mouseMoveFunc {|w, x, y|
		(mouseClickButton == 0).if({
			origin.x = origin.x + x - mouseClickStartX;
			origin.y = origin.y + y - mouseClickStartY;
			w.refresh;
		});
	}
	mouseOverAction {|w, x, y|
		"over %".format(w).postln;
	}


	print{|type|
		switch (type,
			\version, { "\nHistoryTrack [%]".format(version).postln; },
			\lines, { isLoad.if({
				"\nHistoryTrack lines".postln;
				historyLines.do({ |oneLine, index| "\t%) %".format(index, oneLine).postln; }) });
			},
			\duration, { isLoad.if({ "HistoryTrack duration : %".format(this.formatTime(this.duration)).postln; }); },
			\players, { isLoad.if({
				"\nHistoryTrack players".postln;
				this.players.do ({ |player| "\t- %".format(player).postln;  });	});
			},
			\modify, { isLoad.if({ "HistoryTrack modify date : %".format(this.modifyDate).postln; }); },
		)
	}



	formatTime { |val|
		var h, m, s;
		h = val div: (60 * 60);
		val = val - (h * 60 * 60);
		m = val div: 60;
		val = val - (m * 60);
		s = val;
		(m < 10).if ({ m = "0%".format(m.floor); }, { m = m.floor; });
		(s < 10).if ({ s = "0%".format(s.floor); }, { s = s.floor; });
		^"%:%:%".format(h, m, s)
	}

	formatDate {|rawSec, gmtOffset = 2|
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
					{monthDayCnt[1] = 29},
					{monthDayCnt[1] = 28}
				);
				yearDayCnt = monthDayCnt.sum;
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
		^date;
	}

}