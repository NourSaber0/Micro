package logic;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Data {
	private class Cache {
		private static class CacheBlock {
			int cacheTag;
			String data;

			public CacheBlock(String data, int cacheTag) {
				this.data = data;
				this.cacheTag = cacheTag;
			}
		}

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

		public boolean isInCache(int address) {
			int blockIndex = (address / blockSize) % numBlocks;
			int tag = address / blockSize;
			if (cache.containsKey(blockIndex) && cache.get(blockIndex).cacheTag == tag) {
				// Move accessed block to the end of the usage list
				usageOrder.remove((Integer) blockIndex);
				usageOrder.addLast(blockIndex);
				return true;
			}
			return false;
		}

		public boolean isCacheFull() {
			return cache.size() == numBlocks;
		}

		// Get data using address from cache
		public String getData(int address) {
			int blockIndex = (address / blockSize) % numBlocks;
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
				if(cache.get(i) != null) sb.append("[" + i + "]: " + cache.get(i).cacheTag + ", " + cache.get(i).data + "\n");
			}
			return sb.toString();
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
}
