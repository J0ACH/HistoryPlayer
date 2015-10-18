HP_library{
	classvar version = 0.04;

	var parent;
	var origin, sizeX, sizeY;

	var view, objects;
	var tracks;

	*new { |parent|
		^super.newCopyArgs(parent).init;
	}

	init{
		tracks = List.newClear;
		objects = Dictionary.new;
		// tracks = this.folderFiles(this.template[\pathHistory], true, \scd, true, true);

		this.reloadLibraryFolder;
		this.print(\tracks);
		// this.initGUI;
		// this.refreshGUI;
	}

	window { ^parent.window; }
	template { ^parent.template; }

	print{|type|
		switch (type,
			\version, { "\nHistoryPlayer [%]".format(version).postln; },
			\tracks, {
				"\nHistoryPlayer tracks ( % )".format(this.template[\pathHistory]).postln;
				// tracks.do({ |onePath, index| "\t%) %".format(index, PathName(onePath).fileName).postln; })
				tracks.do({ |oneTrack| "\t- %".format(oneTrack.name).postln; })
			},
		)
	}

	reloadLibraryFolder{
		var tracksPaths = this.folderFiles(this.template[\pathHistory], true, \scd, true, true);
		tracks = List.newClear;
		tracksPaths.do({|onePath|
			tracks.add(HP_track(onePath));
		});
		// controls[\playlist_txt].notNil.if ({ controls[\playlist_txt].items_(this.trackName); });

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


	initGUI{ |origin, sizeX, sizeY|
		view = UserView.new(this.window, Rect(origin.x, origin.y, sizeX, sizeY))
		.background_(this.template[\colorBackground]);

		tracks.do({|oneTrack, i|
			oneTrack.historyPath.postln;
			oneTrack.template = this.template;
			oneTrack.initGUI(view, (10@((i*50)+30)), 300, 22);

		});

		objects.put(\label, StaticText( view, Rect.fromPoints((5@5), (50@20)))
			.string_("library")
			.font_(this.template[\fontChapter])
			.stringColor_(this.template[\colorFront])
			.align_(\topLeft)
		);

		objects.put(\playlist_buttonHistoryPath, Button(view,
			Rect.fromPoints((5@(view.bounds.height-40)), (45@(view.bounds.height-25)))
		)
		.font_(this.template[\fontSmall])
		.states_([
			["git", this.template[\colorFront], this.template[\colorBackground]],
			["git", this.template[\colorFront], this.template[\colorActive]]
		])
		);

		objects.put(\playlist_txtHistoryPath, StaticText( view,
			Rect.fromPoints((50@(view.bounds.height-40)), ((view.bounds.width-5)@(view.bounds.height-25)))
		)
		.string_(this.template[\pathHistory])
		.font_(this.template[\fontSmall])
		.stringColor_(this.template[\colorFront])
		);

		objects.put(\playlist_buttonRenderPath, Button(view,
			Rect.fromPoints((5@(view.bounds.height-20)), (45@(view.bounds.height-5)))
		)
		.font_(this.template[\fontSmall])
		.states_([
			["render", this.template[\colorFront], this.template[\colorBackground]],
			["render", this.template[\colorFront], this.template[\colorActive]]
		])
		);

		objects.put(\playlist_txtRenderPath, StaticText( view,
			Rect.fromPoints((50@(view.bounds.height-20)), ((view.bounds.width-5)@(view.bounds.height-5)))
		)
		.string_(this.template[\pathRender])
		.font_(this.template[\fontSmall])
		.stringColor_(this.template[\colorFront])
		);
		^view;
	}

	refreshGUI {
		view.drawFunc = {
			Pen.strokeColor = this.template[\colorFront];
			Pen.addRect(Rect(0,0, view.bounds.width, view.bounds.height));
			Pen.stroke;
		};
	}

}
