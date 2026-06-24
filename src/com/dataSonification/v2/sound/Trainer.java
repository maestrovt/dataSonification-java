package com.dataSonification.v2.sound;

import com.dataSonification.v2.data.DataInfo;

abstract class Trainer extends SonificationComponent {
    abstract DataInfo evaluate();
}