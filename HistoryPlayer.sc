HistoryPlayer{
	classvar version = 0.04;
	classvar serverMemory = 5529600;
	classvar instance = nil;

	var template, playlist;
	var <win, views, controls;

	var clock;
	var currentHistory, pathHistory;
	var lines, currentLine;
	var currentTime, startTime, endTime;
	var currentCode;
	var isRunning, isPlaying, isLoad;

	*new {
		instance.isNil.if(
			{ ^super.new.makeProxy(120).init; },
			{
				"HistoryPlayer instance running".postln;
				instance.win.visible_(true);
				^instance;
			}
		);
	}

	close{ this.print(\exit); instance = nil;}

	init{
		// TempoClock.default_(TempoClock.new(queueSize: 8192).permanent_(true));
		Server.local.options.memSize = serverMemory;
		Server.internal.options.memSize = serverMemory;
		Server.local.waitForBoot({
			instance = this;

			template = Dictionary.new;
			playlist = List.newClear;
			views = Dictionary.new;
			controls = Dictionary.new;

			// currentHistory = nil;
			lines = nil;
			pathHistory = "";

			isRunning = true;
			isPlaying = false;
			isLoad = false;

			this.print(\version);
			this.initFiles(Platform.userAppSupportDir).print(\template).print(\playlist);
			this.initGUI(650,300);
			this.initControl;
		});
	}

	makeProxy{|newTempo|
		var proxy;
		currentEnvironment.clear.pop;
		proxy = ProxySpace.new(Server.local);
		proxy.makeTempoClock;
		proxy.clock.tempo_(newTempo/60);

		proxy.push(currentEnvironment);
	}

	print{|type|
		isRunning.if(
			{
				switch (type,
					\version, { "\nHistoryPlayer [%]".format(version).postln; },
					\exit, { "\nHistoryPlayer closed ...".postln; },
					\template, {
						"\nHistoryPlayer templates".postln;
						template.sortedKeysValuesDo({|key, value| "\t- % || %".format(key, value).postln });
					},
					\lines, { isLoad.if({
						"\nHistoryPlayer lines".postln;
						lines.do({ |oneLine, index| "\t%) %".format(index, oneLine).postln; }) });
					},
					\info, { isLoad.if({
						"\nHistoryPlayer info".postln;
						"\t- pathHistory || %".format(pathHistory).postln;
						"\t- linesCount || %".format(lines.size-1).postln;
						"\t- endTime || %".format(endTime).postln;
					});
					},
					\code, { isLoad.if({
						"\nHistoryPlayer currentCode".postln;
						currentCode.postln;
					});
					},
					\playlist, {
						"\nHistoryPlayer tracks ( % )".format(Platform.userAppSupportDir +/+ "HistoryPlayer" +/+ "HistoryFiles").postln;

						playlist.do({ |onePath, index| "\t%) %".format(index, PathName(onePath).fileName).postln; })
					},
				)
			},
			{ "HistoryPlayer instance not running".postln; }
		);
	}

	// FILE MANAGMENT //////////////////////////////////////////

	initFiles{ |rootFolder|
		var playlistFile = rootFolder +/+ "HistoryPlayer" +/+ "_playlist.scd";
		var templatePath = rootFolder +/+ "HistoryPlayer" +/+ "_template.scd";

		File.mkdir(rootFolder +/+ "HistoryPlayer" +/+ "HistoryFiles");
		File.mkdir(rootFolder +/+ "HistoryPlayer" +/+ "RenderedFiles");

		File.exists(templatePath).if (
			{
				this.readTemplate(templatePath)
			},
			{
				this.newTemplate;
				this.writeTemplate(templatePath, template);
				this.initFiles(rootFolder);
			}
		);

		playlistFile = File(playlistFile, "w");
		playlistFile.close;


		// playlist = this.folderFiles(rootFolder +/+ "HistoryPlayer" +/+ "HistoryFiles", true, \scd, true, true);
		this.refreshPlaylist;
	}

	newTemplate {
		template.put(\colorBackground, Color.new255(30,30,30));
		template.put(\colorFront, Color.new255(255,255,255));
		template.put(\colorActive, Color.new255(80,80,80));
		template.put(\fontTime, Font('Segoe UI', 14, 'true'));
		template.put(\fontChapter, Font('Segoe UI', 10, 'true'));
		template.put(\fontSmall, Font('Segoe UI', 9, 'true'));
		template.put(\opacityWin, 0.85);
		template.put(\pathHistory, (Platform.userAppSupportDir +/+ 'HistoryPlayer' +/+ 'HistoryFiles').asSymbol);
		template.put(\pathRender, (Platform.userAppSupportDir +/+ 'HistoryPlayer' +/+ 'RenderedFiles').asSymbol);
	}

	writeTemplate {|path, template|
		var templateFile = File(path, "w");
		template.sortedKeysValuesDo({|key, value|
			// "\t- % || %".format(key, value).postln;
			templateFile.write("%>%;\n".format(key, value));
		});
		templateFile.close;
		^nil;
	}

	readTemplate {|path|
		File.exists(path).if (
			{
				var templateFile = File(path, "r");
				var fileTxt = templateFile.readAllStringRTF;
				fileTxt = fileTxt.replace("\n", "");
				fileTxt = fileTxt.split($;);
				fileTxt.do({|txt, i|
					var symbol, code, stringPath, re;
					txt.notEmpty.if({
						txt = txt.split($>);
						symbol = txt[0].stripRTF.asSymbol;
						code = txt[1].stripRTF.asString;

						// 2-lines from Quarks *isPath
						re = if(thisProcess.platform.name !== 'windows', "^[~\\.]?/", "^(\\\\|[a-zA-Z]:)");
						(code.findRegexp(re).size != 0).if( { stringPath = code; }, { stringPath = nil } );

						symbol.notNil.if({
							stringPath.isNil.if(
								{ template.put(symbol.asSymbol, code.interpret);  },
								{ template.put(symbol.asSymbol, stringPath.asSymbol); }
							);
						});

					});
				});
				templateFile.close;

				// template.sortedKeysValuesDo({|key, value| "\t- % || %".format(key, value).postln; });

				^nil;
			},
			{ Error("HistoryPlayer template not found").throw; }
		);
	}

	folderFiles {|scanPath, subfolder = false, extensionType = nil, withPath = false, withExtension = true|
		var files = List.newClear;
		var inPath = PathName(scanPath ++ "/");
		// "scan by this.folderFiles %".format(inPath.folderName).postln;
		inPath.filesDo ({|path|
			subfolder.not.if({ (inPath.folderName == path.folderName).not.if({ path = nil; }); });
			path.notNil.if({ extensionType.notNil.if({ (path.extension.asSymbol == extensionType.asSymbol).not.if ({ path = nil; }); }); });
			path.notNil.if({
				withPath.if(
					{ files.add(path.fullPath); },
					{
						withExtension.if(
							{ files.add(path.fileName); },
							{ files.add(path.fileNameWithoutExtension); }
						);
					}
				);
			});
		});
		^files;
	}

	// PLAYER METHOD //////////////////////////////////////////

	openHistory {|openFile = nil|
		openFile.isNil.if(
			{
				File.openDialog (nil, { |path|
					("path "++ path).postln;
					pathHistory = path;
					currentHistory = History.clear.loadCS(path);
					("pathHistory "++pathHistory).postln;
					lines = currentHistory.lines.reverse;
					isLoad = true;

					currentTime = 0;
					currentLine = 0;
					endTime = this.lineTime(lines[lines.size-1]);
					currentCode = this.lineCode(lines[0]);

					this.refreshInfo;
					this.refreshCode;
					this.refreshTime;
					this.print(\info);
				});
			},
			{
				pathHistory = openFile;
				currentHistory = History.clear.loadCS(openFile);
				("pathHistory "++pathHistory).postln;
				lines = currentHistory.lines.reverse;
				isLoad = true;

				currentTime = 0;
				currentLine = 0;
				endTime = this.lineTime(lines[lines.size-1]);
				currentCode = this.lineCode(lines[0]);

				this.refreshInfo;
				this.refreshCode;
				this.refreshTime;
				this.print(\info);
			}
		);
	}

	play{
		clock = TempoClock.new(1, currentTime, queueSize: 8192);
		startTime = currentTime;
		clock.beats = startTime;
		currentCode = this.currentIndex(startTime);
		isPlaying = true;

		lines.do({|oneLine|
			clock.schedAbs(this.lineTime(oneLine), {
				currentCode = this.lineCode(oneLine);
				currentCode.interpret;
				{ this.refreshCode; }.defer;
				nil
			})
		});

		clock.schedAbs(0, { |time|
			currentTime = time;
			{ this.refreshTime; nil; }.defer;
			1
		});

		clock.schedAbs(endTime, {
			clock.stop;
			{ currentTime = endTime; this.stop; }.defer;
			nil
		});
	}

	stop {
		isPlaying.if({
			clock.stop;
			AppClock.clear;
			// currentTime = 0;
			this.refreshTime;

			// currentHistory.stop;
			Server.local.freeAll;
			isPlaying = false;
		});
	}

	lineTime {|lineMsg|	isLoad.if({ ^lineMsg[0]; }); }
	linePlayer {|lineMsg| isLoad.if({ ^lineMsg[1]; }); }
	lineCode {|lineMsg| isLoad.if({ ^lineMsg[2]; }); }

	currentIndex { |time|
		isLoad.if({
			var i = 0;
			var answ = lines.size-1;
			answ = block {|break|
				lines.do {|oneLine, i|
					var tempTime = this.lineTime(oneLine);
					(time <= tempTime).if({ break.value(i); }, { i = i + 1; });
				};
			};
			answ.isInteger.if({^answ}, {^(lines.size-1)});
		});
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

	trackName{
		var list = List.newClear;
		playlist.do({|path|	list.add(PathName(path).fileNameWithoutExtension) });
		^list.asArray;
	}

	refreshTime {
		controls[\clock_playtime].string_(this.formatTime(currentTime));
		controls[\clock_slider].value_(currentTime/endTime);
	}

	refreshCode { controls[\code_txt].string_(currentCode);	}

	refreshPlaylist{
		playlist = this.folderFiles(template[\pathHistory], true, \scd, true, true);
		controls[\playlist_txt].notNil.if ({ controls[\playlist_txt].items_(this.trackName); });

	}

	refreshInfo{

		// INFOBAR //////////////////////////////////////////////

		isLoad.if({
			controls[\info_loadPath].string_("openPath : %".format(pathHistory));
			controls[\info_linesCount].string_("linesCount : %".format(lines.size-1));
			controls[\info_endTime].string_("EndTime : %".format(this.formatTime(endTime)));
			// this.print(\lines);
		});
	}

	initControl{

		// CONTROLBAR //////////////////////////////////////////////

		controls[\control_play].action_({ |button|
			isLoad.if({
				(button.value == 1).if (
					{ this.play; },
					{
						button.value = 0;
						this.stop;
					}
				);
			});
		});

		controls[\control_stop].action_({ |button|
			isLoad.if({
				(button.value == 1).if (
					{
						this.stop;
						controls[\control_play].value = 0;
						AppClock.sched(0.05, {button.value = 0};);
					},
					{ button.value = 0; }
				);
			});
		});

		controls[\control_close].action_({ |button| (button.value == 1).if ({
			this.stop;
			this.writeTemplate(Platform.userAppSupportDir +/+ "HistoryPlayer" +/+ "_template.scd", template);
			win.close;
		});
		});

		controls[\control_open].action_({ |button| (button.value == 1).if({
			// controls[\playlist_txt].value = 0;
			this.stop;
			currentTime = 0;
			currentLine = 0;
			this.openHistory(nil);
			// this.refreshCode;
			// this.play;
			AppClock.sched(0.05, {button.value = 0};);
			controls[\control_play].value = 0;
		}, {
			button.value = 0;
		});
		});

		// TIMEBAR //////////////////////////////////////////////

		controls[\clock_slider].action_({ |slider| isLoad.if(
			{
				this.stop;
				controls[\control_play].value = 0;

				currentTime = slider.value * endTime;
				this.refreshTime;

				currentCode = this.lineCode(lines[this.currentIndex(currentTime)]);
				this.refreshCode;
			},{
				slider.value = 0;
		})
		});

		// PLAYLIST //////////////////////////////////////////////

		controls[\playlist_txt]
		.items_(this.trackName;)
		.enterKeyAction = ({ |plist|
			this.stop;
			currentTime = 0;
			currentLine = 0;
			this.openHistory(playlist[plist.value]);
			this.play;
			controls[\control_play].value = 1;
		});
		controls[\playlist_txt].mouseDownAction = {|me, x, y, mod, buttNum, clickCnt|
			// "mouse [%,%] mod:%, buttNum : %, clickCnt : %".format(x, y, mod, buttNum, clickCnt).postln;
			(clickCnt == 2).if({
				controls[\playlist_txt].value.postln;
				this.stop;
				currentTime = 0;
				currentLine = 0;
				this.openHistory(playlist[controls[\playlist_txt].value]);
				this.play;
				controls[\control_play].value = 1;
			});
		};



		controls[\playlist_buttonHistoryPath]
		.action_({ |button| (button.value == 1).if ({
			FileDialog({|selectedPath|
				var path = selectedPath.standardizePath;
				template[\pathHistory] = path;
				controls[\playlist_txtHistoryPath].string = path;
				this.refreshPlaylist;
			},nil, 2, 0, true );
		});
		button.value = 0;
		});


	}

	initGUI{|winX, winY|
		var viewOriginX, viewOriginY, viewSizeX, viewSizeY;
		var originX, originY, sizeX, sizeY;

		win = Window.new("HistoryPlayer v%".format(version), Rect(winX, winY, 800, 600), false)
		.alwaysOnTop_(true)
		.alpha_(template[\opacityWin])
		.background_(template[\colorBackground])
		.front
		// .userCanClose_(false)
		.onClose_({ this.close; });


		// PLAYLIST //////////////////////////////////////////////

		viewOriginY = 10;
		viewOriginX = (win.bounds.width/2) + 5;
		viewSizeX = win.bounds.width/2 - 5;
		viewSizeY = win.bounds.height - 20;
		views.put(\playlist, UserView(win, Rect.fromPoints( ((win.bounds.width/2)@10), ((win.bounds.width-10)@(win.bounds.height-10))))
			.drawFunc = {
				Pen.strokeColor = template[\colorFront];
				Pen.addRect(Rect(0,0, views[\playlist].bounds.width, views[\playlist].bounds.height));
				Pen.stroke;
		});
		controls.put(\playlist_txt,
			ListView(views[\playlist], Rect.fromPoints( (5@5), ((views[\playlist].bounds.width -5)@(views[\playlist].bounds.height-45))))
			// .stringColor_(template[\fontSmall])
			.background_(template[\colorBackground])
			.stringColor_(template[\colorFront])
			.hiliteColor_(template[\colorActive])
			.selectedStringColor_(template[\colorBackground])
		);


		/*
		controls.put(\playlist_txt,
		EZListView.new(
		views[\playlist],
		Rect.fromPoints((5@5), ((views[\playlist].bounds.width -5)@(views[\playlist].bounds.height-5))),
		// (views[\playlist].bounds.width -5)@(views[\playlist].bounds.height-5),
		"test",
		[
		\item0 ->{ |a| ("this is item 0 of " ++ a).postln },
		\item1 ->{ |a| ("this is item 1 of " ++ a).postln },
		\item2 ->{ |a| ("this is item 2 of " ++ a).postln },
		],
		globalAction: { |a| ("this is a global action of "++a.asString ).postln },
		initVal: 2,
		initAction: true,
		labelWidth: 120,
		labelHeight: 16,
		layout: \vert,
		gap: 2@2
		)

		.setColors(
		stringBackground:template[\colorBackground],
		// stringColor:template[\colorFront],
		// listBackground:template[\colorBackground],
		// listStringColor:template[\colorFront],
		// selectedStringColor:template[\colorBackground],
		// hiliteColor:template[\colorActive],
		background:template[\colorBackground]
		)

		.font_(template[\fontSmall])

		);
		*/

		controls.put(\playlist_buttonHistoryPath, Button(views[\playlist],
			Rect.fromPoints((5@(views[\playlist].bounds.height-40)), (45@(views[\playlist].bounds.height-25)))
		)
		.font_(template[\fontSmall])
		.states_([
			["git", template[\colorFront], template[\colorBackground]],
			["git", template[\colorFront], template[\colorActive]]
		])
		);

		controls.put(\playlist_txtHistoryPath, StaticText( views[\playlist],
			Rect.fromPoints((50@(views[\playlist].bounds.height-40)), ((views[\playlist].bounds.width-5)@(views[\playlist].bounds.height-25)))
		)
		.string_(template[\pathHistory])
		.font_(template[\fontSmall])
		.stringColor_(template[\colorFront])
		);

		controls.put(\playlist_buttonRenderPath, Button(views[\playlist],
			Rect.fromPoints((5@(views[\playlist].bounds.height-20)), (45@(views[\playlist].bounds.height-5)))
		)
		.font_(template[\fontSmall])
		.states_([
			["render", template[\colorFront], template[\colorBackground]],
			["render", template[\colorFront], template[\colorActive]]
		])
		);

		controls.put(\playlist_txtRenderPath, StaticText( views[\playlist],
			Rect.fromPoints((50@(views[\playlist].bounds.height-20)), ((views[\playlist].bounds.width-5)@(views[\playlist].bounds.height-5)))
		)
		.string_(template[\pathRender])
		.font_(template[\fontSmall])
		.stringColor_(template[\colorFront])
		);




		// CODEBAR //////////////////////////////////////////////

		viewOriginX = 10;
		viewSizeX = (win.bounds.width/2) - (2*viewOriginX);
		viewOriginY = 10;
		viewSizeY = 330;
		views.put(\code, UserView(win, Rect( viewOriginX, viewOriginY, viewSizeX, viewSizeY))
			.drawFunc = {
				Pen.strokeColor = template[\colorFront];
				Pen.addRect(Rect(0,0, views[\code].bounds.width, views[\code].bounds.height));
				Pen.stroke;
		});

		controls.put(\code_txt, TextView(views[\code], Rect.fromPoints( (5@5), ((views[\code].bounds.width -5)@(views[\code].bounds.height-5))))
			.font_(template[\fontSmall])
			// .stringColor_(colFront)
			// .background_(colBack)
			.palette_(QPalette.dark)
			.syntaxColorize
			.editable_(false)
			// .hasVerticalScroller_(true)
		);


		// INFOBAR //////////////////////////////////////////////

		viewOriginY = viewOriginY + viewSizeY + 10;
		viewSizeY = 110;
		views.put(\info, UserView(win, Rect( viewOriginX, viewOriginY, viewSizeX, viewSizeY))
			.background_(template[\colorBackground])
			.drawFunc = {
				Pen.strokeColor = template[\colorFront];
				Pen.addRect(Rect(0,0, views[\info].bounds.width, views[\info].bounds.height));
				Pen.stroke;
		});

		controls.put(\info_loadPath, StaticText( views[\info], Rect.fromPoints( (10@10), ((views[\info].bounds.width - 10)@30) ))
			.string_("historyPath : nil")
			.font_(template[\fontSmall])
			.stringColor_(template[\colorFront])
		);

		controls.put(\info_linesCount, StaticText( views[\info], Rect.fromPoints( (10@30),(100@50)))
			.string_("lines : nil")
			.font_(template[\fontSmall])
			.stringColor_(template[\colorFront])
		);

		controls.put(\info_endTime, StaticText( views[\info], Rect.fromPoints( (10@50), (100@70)))
			.string_("EndTime : nil")
			.font_(template[\fontSmall])
			.stringColor_(template[\colorFront])
		);


		// TIMEBAR //////////////////////////////////////////////

		viewOriginY = viewOriginY + viewSizeY + 10;
		viewSizeY = 60;
		views.put(\clock, UserView(win, Rect( viewOriginX, viewOriginY, viewSizeX, viewSizeY))
			.background_(template[\colorBackground])
			.drawFunc = {
				Pen.strokeColor = template[\colorFront];
				Pen.addRect(Rect(0,0, views[\clock].bounds.width, views[\clock].bounds.height));
				Pen.stroke;
		});

		controls.put(\clock_slider, Slider(views[\clock], Rect.fromPoints( (10@40), ((views[\clock].bounds.width - 10)@50) )));

		controls.put(\clock_playtime, StaticText( views[\clock], Rect.fromPoints((10@10), (150@30)))
			.string_("0:00:00")
			.font_(template[\fontTime])
			.stringColor_(template[\colorFront])
		);


		// CONTROLBAR //////////////////////////////////////////////

		viewOriginY = viewOriginY + viewSizeY + 10;
		viewSizeY = 50;
		views.put(\control, UserView(win, Rect( viewOriginX, viewOriginY, viewSizeX, viewSizeY))
			.background_(template[\colorBackground])
			.drawFunc = {
				Pen.strokeColor = template[\colorFront];
				Pen.addRect(Rect(0,0, views[\control].bounds.width, views[\control].bounds.height));
				Pen.stroke;
		});

		sizeX = 30; sizeY = 30;
		originX = 10;
		originY = (views[\control].bounds.height/2) - (sizeY/2);
		controls.put(\control_play, Button(views[\control], Rect(originX, originY, sizeX, sizeY))
			.font_(template[\fontSmall])
			.states_([
				["play", template[\colorFront], template[\colorBackground]],
				["pause", template[\colorFront], template[\colorActive]]
			])
		);

		originX = originX + sizeX + 5;
		controls.put(\control_stop, Button(views[\control], Rect(originX, originY, sizeX, sizeY))
			.font_(template[\fontSmall])
			.states_([
				["stop", template[\colorFront], template[\colorBackground]],
				["stop", template[\colorFront], template[\colorActive]]
			])
		);

		originX = views[\control].bounds.width - sizeX - 10;
		controls.put(\control_close, Button(views[\control], Rect(originX, originY, sizeX, sizeY))
			.font_(template[\fontSmall])
			.states_([
				["close", template[\colorFront], template[\colorBackground]],
				["close", template[\colorFront], template[\colorActive]]
			])
		);

		originX = originX - sizeX - 5;
		controls.put(\control_open, Button(views[\control], Rect(originX, originY, sizeX, sizeY))
			.font_(template[\fontSmall])
			.states_([
				["open", template[\colorFront], template[\colorBackground]],
				["open", template[\colorFront], template[\colorActive]]
			])
		);

	}
}

