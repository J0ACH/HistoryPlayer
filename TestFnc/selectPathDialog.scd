Gitamp()

(
// selethPathDialog
f = {
	var path;
		FileDialog(	{|selectedPath|
		selectedPath.postln;
		path = selectedPath;
	},
	{ path = nil; },
	2,0,false
	);
	path;
}.value
)