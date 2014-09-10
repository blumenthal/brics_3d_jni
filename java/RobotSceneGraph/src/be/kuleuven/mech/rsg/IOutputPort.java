package be.kuleuven.mech.rsg;

/**
 * @brief Generic interface of an output port for updates of the Robot Scene Graph
 */
public interface IOutputPort {
	
	/**
	 * @brief Write data via the implemented mechanism.
	 * @param dataBuffer Binary data to be send. Encoded via HDF5
	 * @param dataLength Length of the buffer.
	 * @return Number of successfully transmitted byes. 
	 *          A value of -1 indicates an error.
	 */
	public int write(byte dataBuffer[], int dataLength);
}
