(
// selethPathDialog
f = {
	var path;
	FileDialog(	{|selectedPath|
		path = PathName(selectedPath ++ "/");
		path.pathOnly.postln;
	},
	{ path = nil; },
	2,0,true
	);
	path.pathOnly;
}.value
)