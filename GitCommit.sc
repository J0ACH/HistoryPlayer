GitCommit{
	*new{|commitName|
		^super.new.init(commitName)
	}
	init{|title|
		"cd C:\/\/github\/gitamp2\/ & test.sh %".format(title).unixCmd;
	}

}