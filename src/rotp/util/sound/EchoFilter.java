/* Based on
DEVELOPING GAME IN JAVA 

Caracteristiques

Editeur : NEW RIDERS 
Auteur : BRACKEEN 
Parution : 09 2003 
Pages : 972 
Isbn : 1-59273-005-1 
Reliure : Paperback 
Disponibilite : Disponible a la librairie 
*/

package rotp.util.sound;

import java.io.IOException;

import javax.sound.sampled.AudioInputStream;

/**
 * The EchoFilter class is a SoundFilter that emulates an echo.
 * 
 * @see FilteredSoundStream
 */
class EchoFilter extends SoundFilter {
	private short[] delayBuffer;
	private int delayBufferPos;
	private float decay;
	/**
	 * Creates an EchoFilter with the specified number of delay samples and the
	 * specified decay rate.
	 * <p>
	 * The number of delay samples specifies how long before the echo is
	 * initially heard. For a 1 second echo with mono, 44100Hz sound, use 44100
	 * delay samples.
	 * <p>
	 * The decay value is how much the echo has decayed from the source. A decay
	 * value of .5 means the echo heard is half as loud as the source.
	 */
	public EchoFilter(int numDelaySamples, float decay) {
		delayBuffer = new short[numDelaySamples];
		this.decay = decay;
	}
	/**
	 * Gets the remaining size, in bytes, of samples that this filter can echo
	 * after the sound is done playing. Ensures that the sound will have decayed
	 * to below 1% of maximum volume (amplitude).
	 */
	@Override public int getRemainingSize() {
		float finalDecay = 0.05f;
		// derived from Math.pow(decay,x) <= finalDecay
		int numRemainingBuffers = (int) Math.ceil(Math.log(finalDecay) / Math.log(decay));
		int bufferSize = delayBuffer.length * 2;
		return bufferSize * numRemainingBuffers;
	}
	/**
	 * Clears this EchoFilter's internal delay buffer.
	 */
	@Override public void reset() {
		for (int i = 0; i < delayBuffer.length; i++) {
			delayBuffer[i] = 0;
		}
		delayBufferPos = 0;
	}
	/**
	 * Filters the sound samples to add an echo. The samples played are added to
	 * the sound in the delay buffer multiplied by the decay rate. The result is
	 * then stored in the delay buffer, so multiple echoes are heard.
	 */
	@Override public void filter(byte[] samples, int offset, int length) {
		for (int i = offset; i < offset + length-1; i += 2) {
			// update the sample
			short oldSample = (short) (getSample(samples, i)/2);
			short newSample = (short) (oldSample + decay * delayBuffer[delayBufferPos]);
			setSample(samples, i, newSample);
			// update the delay buffer
			delayBuffer[delayBufferPos] = newSample;
			delayBufferPos++;
			if (delayBufferPos == delayBuffer.length) {
				delayBufferPos = 0;
			}
		}
	}
}
/**
 * The FilteredSoundStream class is a FilterInputStream that applies a
 * SoundFilter to the underlying input stream.
 * 
 * @see SoundFilter
 */
class FilteredSoundStream extends AudioInputStream {
	private static final int REMAINING_SIZE_UNKNOWN = -1;
	private SoundFilter soundFilter;
	private int remainingSize;
	/**
	 * Creates a new FilteredSoundStream object with the specified InputStream
	 * and SoundFilter.
	 */
	public FilteredSoundStream(AudioInputStream in, SoundFilter soundFilter) {
		super(in, in.getFormat(), in.getFrameLength());
		this.soundFilter = soundFilter;
		remainingSize = REMAINING_SIZE_UNKNOWN;
	}
	/**
	 * Overrides the FilterInputStream method to apply this filter whenever
	 * bytes are read
	 */
	@Override public int read(byte[] samples, int offset, int length) throws IOException {
		// read and filter the sound samples in the stream
		int bytesRead = super.read(samples, offset, length);
		if (bytesRead > 0) {
			soundFilter.filter(samples, offset, bytesRead);
			return bytesRead;
		}
		// if there are no remaining bytes in the sound stream,
		// check if the filter has any remaining bytes ("echoes").
		if (remainingSize == REMAINING_SIZE_UNKNOWN) {
			remainingSize = soundFilter.getRemainingSize();
			// round down to nearest multiple of 4
			// (typical frame size)
			remainingSize = remainingSize / 4 * 4;
		}
		if (remainingSize > 0) {
			length = Math.min(length, remainingSize);
			// clear the buffer
			for (int i = offset; i < offset + length; i++) {
				samples[i] = 0;
			}
			// filter the remaining bytes
			soundFilter.filter(samples, offset, length);
			remainingSize -= length;
			// return
			return length;
		} else {
			// end of stream
			return -1;
		}
	}
}
/**
 * A abstract class designed to filter sound samples. Since SoundFilters may use
 * internal buffering of samples, a new SoundFilter object should be created for
 * every sound played. However, SoundFilters can be reused after they are
 * finished by called the reset() method.
 * <p>
 * Assumes all samples are 16-bit, signed, little-endian format.
 * 
 * @see FilteredSoundStream
 */
abstract class SoundFilter {
	/**
	 * Resets this SoundFilter. Does nothing by default.
	 */
	public void reset() { }	// do nothing
	/**
	 * Gets the remaining size, in bytes, that this filter plays after the sound
	 * is finished. An example would be an echo that plays longer than it's
	 * original sound. This method returns 0 by default.
	 */
	public int getRemainingSize() { return 0; }
	/**
	 * Filters an array of samples. Samples should be in 16-bit, signed,
	 * little-endian format. This method should be implemented by subclasses.
	 */
	public abstract void filter(byte[] samples, int offset, int length);
	/**
	 * Convenience method for getting a 16-bit sample from a byte array. Samples
	 * should be in 16-bit, signed, little-endian format.
	 */
	public static short getSample(byte[] buffer, int position) {
		return (short) (((buffer[position + 1] & 0xff) << 8) | (buffer[position] & 0xff));
	}
	/**
	 * Convenience method for setting a 16-bit sample in a byte array. Samples
	 * should be in 16-bit, signed, little-endian format.
	 */
	public static void setSample(byte[] buffer, int position, short sample) {
		buffer[position] = (byte) (sample & 0xff);
		buffer[position + 1] = (byte) ((sample >> 8) & 0xff);
	}
}	 
