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

	mouseUpFunc {|w, x, y, mod|
		"playlist mouseUpFunc: w:%, x:%, y:%, mod:%".format(w,x,y,mod).postln;



		/*
		isDrag.if({
		// "end of drag %".format(this.name).postln;

		// isDrag = false;
		});
		*/
	}

	mouseEnterAction {|w, x, y|
		"playlist mouseEnter [%,%]".format(x,y).postln;
	}
	mouseLeaveAction {|w, x, y|
		"playlist mouseLeave [%,%]".format(x,y).postln;
	}

	mouseOverAction {|w, x, y|
		// "playlist over [%,%]".format(x,y).postln;
	}


	initGUI{ |origin, sizeX, sizeY|
		view = UserView.new(this.window, Rect(origin.x, origin.y, sizeX, sizeY))
		// .acceptsMouseOver_(true)
				.background_(this.template[\colorBackground]);

		// view.canReceiveDrag = {|x, y| "endDrag2 x:%, y:%".format(x, y).postln;};
		view.canReceiveDragHandler = {View.currentDrag.isNumber}; // what to receive
		view.receiveDragHandler = { View.currentDrag.postln; view.doAction }; // what to do on receiving
		// view.receiveDragHandler = { |w| w.name.postln; view.doAction }; // what to do on receiving

		view.action = ({|w|
			"endDrag %".format(w.name).postln;
		});

		view.mouseUpAction = {|me, x, y, mod| this.mouseUpFunc(me, x, y, mod);  };
		view.mouseOverAction = {|w, x, y| this.mouseOverAction(w, x, y); };
		view.mouseEnterAction = {|w, x, y| /*isMouseOver = true;*/ this.mouseEnterAction;};
		view.mouseLeaveAction = {|w, x, y| /*isMouseOver = false;*/ this.mouseLeaveAction;};

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