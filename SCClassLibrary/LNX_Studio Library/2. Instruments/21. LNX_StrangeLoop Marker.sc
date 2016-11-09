// ************ //
// Marker mode  //
// ************ //

LNX_MarkerEvent {
	var <>offset, <>startFrame, <>durFrame;

	*new {|offset, startFrame, durFrame| ^super.newCopyArgs(offset, startFrame, durFrame) }

	free { offset = startFrame = durFrame = nil }

	printOn {|stream| stream << this.class.name << "(" << offset << "," << startFrame << "," << durFrame << ")" }
}

+ LNX_StrangeLoop {

	initVarsMarker{
		markerSeq = [];
	}
		// method 1
		// loop length is end-start @ playbackRate of 1
		// assume 4 x 4 unless specified
		// compare to absTime to work out nearest multiple of beatLength 64 beats or what ever (but can be set)
		// so then time line is streched to fit this length of n beats.
		// and markers are scheduled to this timeline

		// method 2
		// do as before but this time force a playbackRate of 1
		// launch events at marker point
		// this still need bpm knowledge

	// called from load & ?

	// on load is no good, must be on 1st import?

	// this should only happen on 1st import else length could change

	// this isn't called by anthing yet
	updateVarsMarker{
		var startRatio, endRatio, durRatio, totalDuration, absTime, loopDurTime, length;
		var sampleIndex=p[11];

		if (sampleBank[sampleIndex].isNil) { ^this };		 	// no samples loaded in bank exception

		startRatio    = sampleBank.actualStart(sampleIndex);	// start pos ratio
		endRatio      = sampleBank.actualEnd  (sampleIndex);	// end   pos ratio
		durRatio      = endRatio - startRatio;					// dur ratio
		totalDuration = sampleBank.duration(sampleIndex);		// total dur of sample in secs
		loopDurTime   = totalDuration * durRatio;			 	// total dur of loop in secs
		absTime       = studio.absTime;							// rate of stuido clock ticks
		length		  = (loopDurTime / absTime /3).round.asInt;	// dur of loop in beats, on slower clock

		sampleBank.modelValueAction_(sampleIndex,\length,length,send:false); // set length in buffer metadata

		this.makeMarkerSeq;

	}

	makeMarkerSeq{
		var startRatio, endRatio, durRatio, absTime, length, length3;
		var workingMarkers, workingDurs, numFrames;
		var sampleIndex=p[11];

		if (sampleBank[sampleIndex].isNil) { ^this }; // no samples loaded in bank exception

		startRatio	= sampleBank.actualStart(sampleIndex);	// start pos ratio
		endRatio    = sampleBank.actualEnd  (sampleIndex);	// end   pos ratio
		durRatio    = endRatio - startRatio;				// dur ratio
		absTime		= studio.absTime;						// rate of stuido clock3 ticks\
		length 		= sampleBank.length(sampleIndex).asInt;
		length3		= length * 3;							// this is length of loop in beats on clock3
		numFrames	= sampleBank.numFrames(sampleIndex);	// total number of frames in sample


		// ***
		// so this length needs to be shorten some how ??
		//
		markerSeq	= nil ! ((length3 * durRatio).round(3).asInt); // this is not a good idea

		//markerSeq	= nil ! length3; // now i need to put the markers into this seq, clock dur split up into beats.



		workingMarkers = sampleBank.workingMarkers(sampleIndex); // ratio of buf len
		workingDurs    = sampleBank.workingDurs   (sampleIndex); // ratio of buf len


		//workingMarkers = sampleBank.workingMarkers(sampleIndex).reverse; // ratio of buf len
		//workingDurs    = sampleBank.workingDurs   (sampleIndex).reverse; // ratio of buf len

		// into seq goes:  offset start in sec, startFrame, durFrame
		workingMarkers.do{|marker,j|
			var when  		= (marker - startRatio * length3).round(3); // when will it happen from 0 @ start & length3
			var index  		= when.floor.asInt;							// where to put in the seq index
			var offset		= when.frac;     							// what is frac offset from beat
			var startFrame	= (marker * numFrames).asInt;
			var durFrame	= (workingDurs[j] * numFrames).asInt;

			markerSeq.clipPut(index, LNX_MarkerEvent(offset, startFrame, durFrame));

		};

		~m = markerSeq;

	}

	// repitch mode
	clockInMarker{|instBeat3,absTime3,latency,beat3|
		var length3, markerEvent, instBeat;
		var sampleIndex=p[11];						  // sample used in bank
		if (this.isOff) { ^this };                    // inst is off exception
		if (sampleBank[sampleIndex].isNil) { ^this }; // no samples loaded in bank exception
		instBeat = instBeat3/3; 					  // use inst beat at slower rate
		length3 = sampleBank.length(sampleIndex).asInt * 3; // this is length of loop in beats on clock3

//		markerEvent = markerSeq[instBeat3 % length3];
		markerEvent = markerSeq.wrapAt(instBeat3);

		// pos index (all the time for the moment)
		if (instBeat%2==0) {};

		// launch at start pos or relaunch sample if needed
		if (markerEvent.notNil) {
			var numFrames, bufferL, bufferR, duration, offset, startFrame, endFrame, durFrame, attackLevel;
			var sample      = sampleBank[sampleIndex];
			var rate		= (p[12]+p[13]).midiratio.round(0.0000000001);
			var clipMode    = p[14];
			bufferL			= sample.buffer.bufnum(0);          	// this only comes from LNX_BufferArray
			bufferR			= sample.buffer.bufnum(1) ? bufferL; 	// this only comes from LNX_BufferArray

			{
			this.playBuffeMarker(bufferL,bufferR,rate,markerEvent.startFrame,markerEvent.durFrame,1,clipMode,latency);
				nil;
			}.sched(markerEvent.offset * absTime3);

			//[instBeat3 % length3, markerEvent].postln;

		};

		// change pos rate if bpm changed
		if (newBPM and:{node.notNil}) {};

	}

	// sample bank has changed...so do this
	updateMarker{|model|
		var sampleIndex=sampleBank.selectedSampleNo;  // sample used in bank
		if (sampleBank[sampleIndex].isNil) { ^this }; // no samples loaded in bank exception
		//model.postln;
		if ((model==\start)||(model==\end)) {
			this.makeMarkerSeq;
			relaunch = true; // modelValueAction_ may do a relaunch as well but not always
		};
		if (model==\length) {
			this.makeMarkerSeq;
			relaunch = true;
		};
		if (model==\markers) {
			this.makeMarkerSeq;
			relaunch = true;
		};
	}

	// change playback rate
	changeRateMarker{|latency|
		server.sendBundle(latency +! syncDelay,[\n_set, node, \rate, (p[12]+p[13]).midiratio.round(0.0000000001)]);
	}

	// play a buffer
	playBuffeMarker{|bufnumL,bufnumR,rate,startFrame,durFrame,attackLevel,clipMode,latency|

		this.stopBufferMarker(latency);
		node = server.nextNodeID;

		server.sendBundle(latency +! syncDelay, ["/s_new", \SLoopMarker, node, 0, instGroupID,
			\outputChannels, this.instGroupChannel,
			\id,id,
			\bufnumL,bufnumL,
			\bufnumR,bufnumR,
			\rate,rate,
			\startFrame,startFrame,
			\durFrame:durFrame,
			\attackLevel:attackLevel,
			\clipMode:clipMode
		]);

	}

	*initUGensMarker{|server|

		SynthDef("SLoopMarker",{|outputChannels=0,bufnumL=0,bufnumR=0,rate=1,startFrame=0,durFrame=44100,
				gate=1,attackLevel=1, clipMode=0, id=0|
			var signal;
			var index  = Integrator.ar((rate * BufRateScale.ir(bufnumL)).asAudio);

			index = startFrame +
				Select.ar(clipMode,[ index.clip(0,durFrame) , index.fold(0,durFrame) , index.wrap(0,durFrame) ]);


			signal = BufRd.ar(1, [bufnumL,bufnumR], index ,loop:0); // mono, might need to be leaked
			signal = signal * EnvGen.ar(Env.new([attackLevel,1,0], [0.01,0.01], [2,-2], 1),gate,doneAction:2);

			DetectSilence.ar(Slope.ar(index), doneAction:2); // ends when index slope = 0
			OffsetOut.ar(outputChannels,signal);			 // now send out

			SendReply.kr(Impulse.kr(20), '/sIdx', [index], id); // send sample index back to client

		}).send(server);

	}

	sIdx_in_{|index|
		var sampleIndex = p[11];
		var numFrames	= sampleBank.numFrames(sampleIndex);	// total number of frames in sample
		sampleBank.otherModels[sampleIndex][\pos].valueAction_(index/numFrames);
	}

	// stop playing buffer
	stopBufferMarker{|latency|
		if (node.notNil) {
			server.sendBundle(latency +! syncDelay, ["/n_set", node, \gate, 0])
			//server.sendBundle(latency +! syncDelay, ["/n_free", node] )
		};
	}

}
