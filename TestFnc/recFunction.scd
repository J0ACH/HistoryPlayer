
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
	SynthDef(\renderHistory, { |buffer|	RecordBuf.ar(In.ar(0,2), buffer, loop: 0, doneAction: 2); }).add;

	Routine({
		var serverRecord = Server.new(\record, NetAddr("127.0.0.1", 57110));

		var outFile = Platform.userAppSupportDir +/+ "GitAmp" +/+ "RenderedFiles" +/+ "test2.flac";
		// var replayGroup = Group.new(nil, \addToTail);
		var recGroup = Group.after(Server.local.defaultGroup);
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
			duration/10;
			// recProxy
		});
		renderClock.sched((duration+0.5), { renderClock.stop; "render done ....".postln;});


		// recBuf = Buffer.alloc(Server.local, 262144, 2);
		recBuf = Buffer.alloc(Server.local, Server.local.sampleRate * duration, 2);

		Server.local.sync;
		// playObject = Synth.new( \playHistory, target: recGroup);
		recObject = Synth.new( \renderHistory, [\buffer, recBuf], recGroup);

		// recObject = Synth.new( \recordTone, [\buffer, recBuf], recGroup.nodeID);
		// recObject.moveToTail(nil);
		// nodeHistory.source = History.current.player;
		// History.current.player.player.postln;
		// ("pg : " ++ History.current.player.player).postln;

		// ~player.play;
		History.current.play;
		// playObject.play;P
		duration.wait;

		recBuf.write(
			outFile,
			"flac",
			"int16",
			completionMessage: ["/b_free", recBuf]
		);

		1.wait;
		// recObject.free;
		// "leaving proxTBy".warn;
		// recProxy.pop;

	}).next;

};

f.value(Platform.userAppSupportDir +/+ "GitAmp" +/+ "HistoryFiles" +/+ "test.scd");
)

History.loadCS(Platform.userAppSupportDir +/+ "GitAmp" +/+ "HistoryFiles" +/+ "test.scd")
History.current.lines

Server.all
