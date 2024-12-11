package logic;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Data {
	private final Cache cache;
	private final Memory memory;

	public Data(int cacheSize, int blockSize, int hitLatency, int missPenalty, int memorySize) {
		this.cache = new Cache(cacheSize, blockSize, hitLatency, missPenalty);
		this.memory = new Memory(memorySize);
	}

	public int getLatency(int address) {
		return cache.isInCache(address) ? cache.hitLatency : cache.missPenalty;
	}

	public String read(int address) {
		if (cache.isInCache(address)) {
			return cache.getData(address);
		} else {
			String data = memory.read(address);
			if (!cache.isCacheFull()) {
				cache.writeData(address, data);
			}
			return data;
		}
	}

	public void write(int address, String data) {
		memory.write(address, data);
		if (!cache.isInCache(address)) {
			if (!cache.isCacheFull()) {
				cache.writeData(address, data);
			} else {
				// Evict a block based on least used.
				int lruBlockIndex = cache.usageOrder.removeFirst();
				cache.cache.remove(lruBlockIndex);
				cache.writeData(address, data);
			}
		}
	}

	public int getCacheSize() {
		return cache.getCacheSize();
	}

	public int getCacheBlockSize() {
		return cache.getBlockSize();
	}

	public int getMemorySize() {
		return memory.size;
	}

	public String toString() {
		return "Cache:\n" + cache + "\nMemory:\n" + memory;
	}

	private class Cache {
		private final int blockSize;
		private final int cacheSize;
		private final int numBlocks;
		private final Map<Integer, CacheBlock> cache; // Maps block index to cache block
		private final LinkedList<Integer> usageOrder; // Keeps track of usage order
		private int hitLatency;
		private int missPenalty;

		public Cache(int cacheSize, int blockSize, int hitLatency, int missPenalty) {
			this.cacheSize = cacheSize;
			this.blockSize = blockSize;
			this.numBlocks = cacheSize / blockSize;
			this.cache = new HashMap<>();
			this.usageOrder = new LinkedList<>();
			this.hitLatency = hitLatency;
			this.missPenalty = missPenalty;
		}

		public int getBlockSize() {
			return blockSize;
		}

		public int getCacheSize() {
			return cacheSize;
		}

		//issue calculating tag
		public boolean isInCache(int address) {
			int blockIndex = (address / blockSize) % numBlocks;
			int tag = address / blockSize;
			return cache.containsKey(blockIndex) && cache.get(blockIndex).cacheTag == tag;
		}

		public boolean isCacheFull() {
			return cache.size() == numBlocks;
		}

		// Get data using address from cache
		public String getData(int address) {
			int blockIndex = (address / blockSize) % numBlocks;
			// Move accessed block to the end of the usage list
			usageOrder.remove((Integer) blockIndex);
			usageOrder.addLast(blockIndex);

			return cache.get(blockIndex).data;
		}

		// Write data to cache using address
		public void writeData(int address, String data) {
			int blockIndex = (address / blockSize) % numBlocks;
			int tag = address / blockSize;

			if (isCacheFull() && !cache.containsKey(blockIndex)) {
				// Evict the least recently used block
				int lruBlockIndex = usageOrder.removeFirst();
				cache.remove(lruBlockIndex);
			}

			// Add new block to cache and update usage order
			cache.put(blockIndex, new CacheBlock(data, tag));
			usageOrder.remove((Integer) blockIndex);
			usageOrder.addLast(blockIndex);
		}

		public String toString() {
			// return cache as such [index]: tag, data
			StringBuilder sb = new StringBuilder();
			for (Integer i : cache.keySet()) {
				if (cache.get(i) != null)
					sb.append("[" + i + "]: " + cache.get(i).cacheTag + ", " + cache.get(i).data + "\n");
			}
			return sb.toString();
		}

		private static class CacheBlock {
			int cacheTag;
			String data;

			public CacheBlock(String data, int cacheTag) {
				this.data = data;
				this.cacheTag = cacheTag;
			}
		}
	}

	private class Memory {
		private final int size;
		private final String[] data;

		public Memory(int size) {
			this.size = size;
			this.data = new String[size];
			for (int i = 0; i < size; i++) {
				data[i] = Integer.toString((int) (Math.random() * 100));
			}
		}

		public void write(int address, String value) {
			data[address] = value;
		}

		public String read(int address) {
			return data[address];
		}

		public String toString() {
			// return array as such [index]: value
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < size; i++) {
				sb.append("[" + i + "]: " + data[i] + "\n");
			}
			return sb.toString();
		}
	}
}
// direct mapped cache but not completed
//import java.util.*;
//
//public class Data {
//	private class Cache {
//		private static class CacheBlock {
//			String tag; // Tag for the block
//			String[] data; // Data inside the block
//			boolean valid; // Valid bit to check if the block contains valid data
//
//			public CacheBlock(int blockSize) {
//				this.tag = null; // Initially no tag
//				this.data = new String[blockSize]; // Data storage
//				Arrays.fill(this.data, "0"); // Initialize block with zeros
//				this.valid = false; // Initially invalid
//			}
//		}
//
//		private final int blockSize;
//		private final int numBlocks;
//		private final int offsetBits;
//		private final int indexBits;
//		private final int hitLatency;
//		private final int missPenalty;
//		private final CacheBlock[] blocks; // Array of cache blocks
//
//		public Cache(int cacheSize, int blockSize, int hitLatency, int missPenalty, int memorySize) {
//    this.blockSize = blockSize;
//    this.hitLatency = hitLatency;
//    this.missPenalty = missPenalty;
//
//    // Number of blocks in the cache
//    this.numBlocks = cacheSize / blockSize;
//
//    // Bits required for offset and index
//    this.offsetBits = (int) (Math.log(blockSize) / Math.log(2));
//    this.indexBits = (int) (Math.log(numBlocks) / Math.log(2));
//
//    // Initialize cache blocks
//    this.blocks = new CacheBlock[numBlocks];
//    for (int i = 0; i < numBlocks; i++) {
//        blocks[i] = new CacheBlock(blockSize);
//    }
//
//    // Set memory size for dynamic address computation
//    this.memorySize = memorySize;
//}
//
//		// Extract the offset, index, and tag from the address
//		private String[] getAddressComponents(int address) {
//    // Calculate address size in bits based on memory size
//    int addressSize = (int) Math.ceil(Math.log(memory.size) / Math.log(2)); // log2(memorySize)
//
//    // Convert address to binary and pad it to addressSize bits
//    String binaryAddress = Integer.toBinaryString(address);
//    binaryAddress = String.format("%" + addressSize + "s", binaryAddress).replace(' ', '0');
//
//    // Extract tag, index, and offset
//    String offset = binaryAddress.substring(addressSize - offsetBits); // Offset bits
//    String index = binaryAddress.substring(addressSize - offsetBits - indexBits, addressSize - offsetBits); // Index bits
//    String tag = binaryAddress.substring(0, addressSize - offsetBits - indexBits); // Remaining bits as tag
//
//    return new String[]{tag, index, offset};
//}
//
//		public boolean isInCache(int address) {
//			String[] components = getAddressComponents(address);
//			int blockIndex = Integer.parseInt(components[1], 2); // Convert index to integer
//			String tag = components[0];
//
//			CacheBlock block = blocks[blockIndex];
//			return block.valid && block.tag.equals(tag); // Valid bit and tag match
//		}
//
//		public String getData(int address) {
//			String[] components = getAddressComponents(address);
//			int blockIndex = Integer.parseInt(components[1], 2); // Convert index to integer
//			int offset = Integer.parseInt(components[2], 2); // Convert offset to integer
//
//			return blocks[blockIndex].data[offset]; // Return the data from the block
//		}
//
//		public void writeData(int address, String data, String[] blockData) {
//			String[] components = getAddressComponents(address);
//			int blockIndex = Integer.parseInt(components[1], 2); // Convert index to integer
//			String tag = components[0];
//			int offset = Integer.parseInt(components[2], 2); // Convert offset to integer
//
//			CacheBlock block = blocks[blockIndex];
//			block.valid = true; // Mark block as valid
//			block.tag = tag; // Update tag
//			block.data = blockData; // Replace the entire block data
//			block.data[offset] = data; // Update specific offset with new data
//		}
//
//		public String toString() {
//			StringBuilder sb = new StringBuilder();
//			for (int i = 0; i < blocks.length; i++) {
//				sb.append("Block ").append(i).append(": ");
//				if (blocks[i].valid) {
//					sb.append("[Tag: ").append(blocks[i].tag).append(", Data: ").append(Arrays.toString(blocks[i].data)).append("]");
//				} else {
//					sb.append("[Invalid]");
//				}
//				sb.append("\n");
//			}
//			return sb.toString();
//		}
//	}
//
//	private class Memory {
//		private final int size;
//		private final String[] data;
//
//		public Memory(int size) {
//			this.size = size;
//			this.data = new String[size];
//			for (int i = 0; i < size; i++) {
//				data[i] = Integer.toString((int) (Math.random() * 100));
//			}
//		}
//
//		public void write(int address, String value) {
//			data[address] = value;
//		}
//
//		public String read(int address) {
//			return data[address];
//		}
//
//		public String[] readBlock(int address, int blockSize) {
//			int start = (address / blockSize) * blockSize; // Starting address of the block
//			String[] blockData = new String[blockSize];
//			for (int i = 0; i < blockSize; i++) {
//				blockData[i] = data[start + i];
//			}
//			return blockData;
//		}
//
//		public String toString() {
//			StringBuilder sb = new StringBuilder();
//			for (int i = 0; i < size; i++) {
//				sb.append("[").append(i).append("]: ").append(data[i]).append("\n");
//			}
//			return sb.toString();
//		}
//	}
//
//	private final Cache cache;
//	private final Memory memory;
//
//	public Data(int cacheSize, int blockSize, int hitLatency, int missPenalty, int memorySize) {
//		this.cache = new Cache(cacheSize, blockSize, hitLatency, missPenalty);
//		this.memory = new Memory(memorySize);
//	}
//
//	public int getLatency(int address) {
//		return cache.isInCache(address) ? cache.hitLatency : cache.missPenalty;
//	}
//
//	public String read(int address) {
//		if (cache.isInCache(address)) {
//			return cache.getData(address); // Cache hit
//		} else {
//			// Cache miss: Load block from memory
//			String[] blockData = memory.readBlock(address, cache.blockSize);
//			cache.writeData(address, null, blockData); // Write block into cache
//			return memory.read(address); // Return the specific address data
//		}
//	}
//
//	public void write(int address, String data) {
//		memory.write(address, data); // Always update memory
//		if (cache.isInCache(address)) {
//			// If address is in cache, update it
//			String[] blockData = memory.readBlock(address, cache.blockSize);
//			cache.writeData(address, data, blockData);
//		}
//	}
//
//	public String toString() {
//		return "Cache:\n" + cache + "\nMemory:\n" + memory;
//	}
//}
