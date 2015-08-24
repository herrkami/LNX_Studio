
// Control groups env's for mapping purposes. useful for difficult parameters
/*
LNX_ControlMap.edit(\moogLadderQAmp);
LNX_ControlMap.dump(\moogLadderQAmp);

LNX_ControlMap.edit(\DFMhpQAmp);
LNX_ControlMap.dump(\DFMhpQAmp);

*/

LNX_ControlMap {

	classvar <controlDict;
	
	*initClass {
	
		controlDict=IdentityDictionary[];
		
		// moogFF
	
		controlDict[\moogFFQ] = Env( [ 0, 0.23247104883194, 0.40951347351074, 0.55392923951149, 0.66426354646683, 0.75372503697872, 0.79344134032726, 0.82982032001019, 0.90244376659393, 1 ], [ 0.058551783125853, 0.086929425373572, 0.13467852708262, 0.15456874833195, 0.14919025271964, 0.13777961944069, 0.13830464112692, 0.099773641405159, 0.040223361393593 ], 'lin',1);
		
		controlDict[\moogFFQAmp] = Env( [ 0.34357541799545, 0.48530876636505, 0.67200666666031, 0.86592178046703, 0.96549901366234, 1, 0.95144193992019, 0.84603653848171, 0.69553071260452 ], [ 0.047561528296444, 0.11254719782142, 0.1831949329909, 0.2629170764092, 0.25252908035364, 0.087558511915154, 0.045236257201466, 0.0084554150117843 ], 'lin',1);
		
		// moogLadder
		
		controlDict[\moogLadderQ] = Env( [ 0, 0.31389099359512, 0.49586302042007, 0.59027633070946, 0.62960892915726, 0.69720673561096, 0.77413558959961, 0.85468821227551, 1 ], [ 0.06170919620791, 0.16127132437083, 0.24007249544453, 0.23466704810798, 0.12551717918012, 0.092616587377793, 0.049463964707359, 0.034682204603483 ], 'lin',1);
		
		controlDict[\moogLadderQAmp] = Env( [ 0.4469273686409, 0.50605466961861, 0.6383059322834, 0.8, 0.96089385449886, 1, 0.97036086209118, 0.90502793341875 ], [ 0.20359354711777, 0.1813528391236, 0.13937793808294, 0.11762043306263, 0.16616335394662, 0.14937340081071, 0.042518487855709 ], 'lin',1);
		
		// DFMlpQ
		
		controlDict[\DFMlpQ] = Env([ 0, 0.95024878904223, 0.95551018416882, 0.97444171272218, 1 ], [ 0.9512158518654, 0.021661662901353, 0.017841098766912, 0.0092813864663267 ], 'lin', 1, nil);
		
		controlDict[\DFMlpQAmp] = Env([ 1, 0.93368119001389, 0.78521375358105, 0.68257206678391, 0.56853693723679, 0.47373461723328, 0.37888413667679, 0.28347450494766, 0.14454090595245 ], [ 0.12845105000189, 0.15375638354768, 0.16299709634365, 0.18659127625202, 0.12163231869699, 0.13007183413898, 0.071668979231876, 0.044831061786904 ], 'lin', 8, nil);
		
		// DFMhpQ
		
		controlDict[\DFMhpQ] = Env([ 0, 0.95024878904223, 0.95551018416882, 0.97444171272218, 1 ], [ 0.9512158518654, 0.021661662901353, 0.017841098766912, 0.0092813864663267 ], 'lin', 1, nil);
		
		controlDict[\DFMhpQAmp] = Env([ 1, 0.59018570184708, 0.47694396972656, 0.36198097467422, 0.25698322057724 ], [ 0.76081839620068, 0.14710853619282, 0.065861366314595, 0.026211701291899 ], 'lin', 4, nil);
		
		// =Env([0,1],[1],'lin',1); // straight line segment
		
	}
	
	*value{|control,val|
		^(controlDict.at(control).at(val))
	}
	
	*env{|control|
		^controlDict.at(control)
	}
	
	*dump{|control|
		this.env(control).postcs
	}
	
	*edit{|control|
		var window,env;
		window=SCWindow.new("Edit control:"+(control.asString)).front;
		env=this.env(control);
		LNX_EnvelopeEdit.new(window,Rect(10,10,window.bounds.width-30,window.bounds.height-30),env, 100)
			.background_(Color(0.7,0.7,0.7))
			.focusColor_(Color(0.3,0.3,0.3,0.35))
			.resize_(5)
			.updateFunc_{|e| env=e };
	}
	
}

