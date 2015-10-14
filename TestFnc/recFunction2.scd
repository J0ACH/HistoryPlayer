
// recordHistory
(
f = {|historyFile|
	// var recProxy = ProxySpace.push(Server.local);
	// recProxy.group = Group.after(Server.local.defaultGroup);
	// currentEnvironment.group = Group.after(Server.local.defaultGroup);
	/*
	SynthDef(\playHistory, {
	var history = History.loadCS(historyFile);
	History.current.play;
	}).add;
	*/
	var serverRecord = Server.new(\record, NetAddr("127.0.0.1", 57110));

	serverRecord.options.protocol = \udp;
	// s.options.maxLogins = 10; // coment out this line if causing error
	serverRecord.options.initialNodeID = 1000;
	serverRecord.options.zeroConf = false;
	serverRecord.queryAllNodes;

	serverRecord.initTree;
	g = {Group.new(serverRecord.defaultGroup)};
	ServerTree.add(g);

	s.sendBundle(["/g_dumpTree", 0, 0]);
	s.queryAllNodes;

	serverRecord.boot;
	Server.local = serverRecord;
	Server.default = serverRecord;
	Server.internal = serverRecord;

	serverRecord.sendBundle(0.1,[\sync,1]);
	serverRecord.sendBundle(0.2,[\notify,1]);

	serverRecord.makeWindow;



	SynthDef(\renderHistory, { |buffer|	RecordBuf.ar(In.ar(serverRecord.outputBus,2), buffer, loop: 0, doneAction: 2); }).add;

	serverRecord.bind {

		Routine({

			var outFile = Platform.userAppSupportDir +/+ "GitAmp" +/+ "RenderedFiles" +/+ "test3.flac";
			// var replayGroup = Group.new(nil, \addToTail);
			var recGroup = Group.after(serverRecord.defaultGroup);
			// var renderPath = "c:\\test";
			var recBuf, recObject, playObject;
			var history = History.loadCS(historyFile);
			var lines = history.lines;
			// var nodeHistory = NodeProxy.audio(s, 2);
			// nodeHistory.play; //listen to b
			var renderClock = TempoClock.new(1);
			var renderPerc = 0;
			var duration = lines[0][0].asFloat + 5;



			// currentEnvironment.group = recGroup;
			lines.printAll;
			duration.postln;
			// History.current.player;
			// History.current.player.envir.name.group = recGroup;
			// Server.local.defaultGroup = recGroup;
			// SynthDef(\renderHistory, { |buffer|	RecordBuf.ar(In.ar(0,2), buffer, loop: 0, doneAction: 2); }).add;

			renderClock.sched(0, {
				"renderProccess : %perc".format(renderPerc).postln;
				renderPerc = renderPerc + 10;
				serverRecord.queryAllNodes;
				duration/10;

				// recProxy
			});
			renderClock.sched((duration+0.5), { renderClock.stop; "render done ....".postln;});


			// recBuf = Buffer.alloc(Server.local, 262144, 2);
			// recBuf = Buffer.alloc(Server.local, Server.local.sampleRate * duration, 2);
			recBuf = Buffer.alloc(serverRecord, serverRecord.sampleRate * duration, 2);

			// Server.local.sync;
			// playObject = Synth.new( \playHistory, target: recGroup);
			recObject = Synth.new( \renderHistory, [\buffer, recBuf], recGroup);

			// recObject = Synth.new( \recordTone, [\buffer, recBuf], recGroup.nodeID);
			// recObject.moveToTail(nil);
			// nodeHistory.source = History.current.player;
			// History.current.player.player.postln;
			// ("pg : " ++ History.current.player.player).postln;

			// ~player.play;
			// serverRecord.makeBundle(0,{ History.current.play;});
			History.current.play;
			// serverRecord.mute;
			// playObject.play;P
			duration.wait;

			recBuf.write(
				outFile,
				"flac",
				"int16",
				completionMessage: ["/b_free", recBuf]
			);

			1.wait;
			serverRecord.freeAll.quit;
			// recObject.free;
			// "leaving proxTBy".warn;
			// recProxy.pop;

		}).next;
	};

};

f.value(Platform.userAppSupportDir +/+ "GitAmp" +/+ "HistoryFiles" +/+ "test.scd");
)

History.loadCS(Platform.userAppSupportDir +/+ "GitAmp" +/+ "HistoryFiles" +/+ "test.scd")
History.current.lines
History.current.player.source.do{|e| e.def.postcs };

Server.all

