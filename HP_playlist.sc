HP_playlist{
	classvar version = 0.04;

	var parent;
	var origin, sizeX, sizeY;

	var view, objects;
	var tracks;

	*new { |parent|
		^super.newCopyArgs(parent).init;
	}

	init{
		objects = Dictionary.new;

		// this.initGUI;
		// this.refreshGUI;
	}

	window { ^parent.window; }
	template { ^parent.template; }

	initGUI{ |origin, sizeX, sizeY|
		view = UserView.new(this.window, Rect(origin.x, origin.y, sizeX, sizeY))
		.background_(this.template[\colorBackground]);

		objects.put(\label, StaticText( view, Rect.fromPoints((5@5), (50@20)))
			.string_("playlist")
			.font_(this.template[\fontChapter])
			.stringColor_(this.template[\colorFront])
			.align_(\topLeft)
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