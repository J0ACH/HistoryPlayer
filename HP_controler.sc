HP_controler{
	classvar version = 0.04;

	var parent;
	var origin, sizeX, sizeY;

	var view, objects;


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
		var buttonSizeX = 30;
		var buttonSizeY = 30;

		view = UserView.new(this.window, Rect(origin.x, origin.y, sizeX, sizeY))
		.background_(this.template[\colorBackground]);

		objects.put(\label, StaticText( view, Rect.fromPoints((5@5), (50@20)))
			.string_("controler")
			.font_(this.template[\fontChapter])
			.stringColor_(this.template[\colorFront])
			.align_(\topLeft)
		);

		objects.put(\code_txt, TextView(view, Rect.fromPoints( (5@30), ((view.bounds.width-5)@350)))
			.font_(this.template[\fontSmall])
			// .stringColor_(colFront)
			// .background_(colBack)
			.palette_(QPalette.dark)
			.syntaxColorize
			.editable_(false)
			// .hasVerticalScroller_(true)
		);

		objects.put(\info_loadPath, StaticText( view, Rect.fromPoints( (10@375), ((view.bounds.width - 10)@390) ))
			.string_("historyPath : nil")
			.font_(this.template[\fontSmall])
			.stringColor_(this.template[\colorFront])
			.align_(\topLeft)
		);

		objects.put(\info_linesCount, StaticText( view, Rect.fromPoints( (10@395),(100@410)))
			.string_("lines : nil")
			.font_(this.template[\fontSmall])
			.stringColor_(this.template[\colorFront])
			.align_(\topLeft)
		);

		objects.put(\info_endTime, StaticText( view, Rect.fromPoints( (10@415), (100@430)))
			.string_("EndTime : nil")
			.font_(this.template[\fontSmall])
			.stringColor_(this.template[\colorFront])
			.align_(\topLeft)
		);

		objects.put(\clock_playtime, StaticText( view, Rect.fromPoints((10@460), (150@480)))
			.string_("0:00:00")
			.font_(this.template[\fontTime])
			.stringColor_(this.template[\colorFront])
			.align_(\topLeft)
		);

		objects.put(\clock_slider, Slider(view, Rect.fromPoints( (10@490), ((view.bounds.width - 10)@500) )));

		objects.put(\control_play, Button(view, Rect(5, view.bounds.height - 35, buttonSizeX, buttonSizeY))
			.font_(this.template[\fontSmall])
			.states_([
				["play", this.template[\colorFront], this.template[\colorBackground]],
				["pause", this.template[\colorFront], this.template[\colorActive]]
			])
		);

		objects.put(\control_stop, Button(view, Rect(40, view.bounds.height - 35, buttonSizeX, buttonSizeY))
			.font_(this.template[\fontSmall])
			.states_([
				["stop", this.template[\colorFront], this.template[\colorBackground]],
				["stop", this.template[\colorFront], this.template[\colorActive]]
			])
		);

		objects.put(\control_close, Button(view, Rect(75, view.bounds.height - 35, buttonSizeX, buttonSizeY))
			.font_(this.template[\fontSmall])
			.states_([
				["close", this.template[\colorFront], this.template[\colorBackground]],
				["close", this.template[\colorFront], this.template[\colorActive]]
			])
		);

		objects.put(\control_open, Button(view, Rect(110, view.bounds.height - 35, buttonSizeX, buttonSizeY))
			.font_(this.template[\fontSmall])
			.states_([
				["open", this.template[\colorFront], this.template[\colorBackground]],
				["open", this.template[\colorFront], this.template[\colorActive]]
			])
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

