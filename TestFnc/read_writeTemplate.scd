(
// writeTemplate
var template = Dictionary.new;

template.put(\colorBackground, Color.new255(30,30,30));
template.put(\colorFront, Color.new255(255,255,255));
template.put(\colorActive, Color.new255(80,80,80));
template.put(\fontTime, Font('Segoe UI', 14, true));
template.put(\fontChapter, Font('Segoe UI', 10,true));
template.put(\fontSmall, Font('Segoe UI', 9,true));
template.put(\opacityWin, 0.85);
template.put(\pathHistory, (Platform.userAppSupportDir +/+ 'HistoryPlayer' +/+ 'HistoryFiles').asSymbol);
template.put(\pathRender, (Platform.userAppSupportDir +/+ 'HistoryPlayer' +/+ 'RenderedFiles').asSymbol);


f = {|path, template|
	var templateFile = File(path, "w");
	template.sortedKeysValuesDo({|key, value|
		"\t- % || %".format(key, value).postln;
		templateFile.write("%>%;\n".format(key, value));
	});
	templateFile.close;
	nil;

}.value("c:/templateTest.scd", template);
)

(
// readTemplate
var template = Dictionary.new;

f = {|path|
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

			template.sortedKeysValuesDo({|key, value| "\t- % || %".format(key, value).postln; });

			nil;
		},
		{ Error("HistoryPlayer template not found").throw; }
	);
}.value("c:/templateTest.scd");

)
