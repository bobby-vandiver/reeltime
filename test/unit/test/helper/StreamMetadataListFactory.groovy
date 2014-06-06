package test.helper

import in.reeltime.metadata.StreamMetadata

class StreamMetadataListFactory {

    static List<StreamMetadata> createRequiredStreams() {
        [
                new StreamMetadata(codecName: 'h264', duration: '130.0021'),
                new StreamMetadata(codecName: 'aac', duration: '129.491033')
        ]
    }
}
