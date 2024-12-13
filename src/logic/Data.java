package logic;

import java.util.*;

public class Data {
	private final Cache cache;
	private final Memory memory;

	public Data(int cacheSize, int blockSize, int hitLatency, int missPenalty, int memorySize) {
		this.cache = new Cache(cacheSize, blockSize, hitLatency, missPenalty);
		this.memory = new Memory(memorySize);
	}

	public Data(Data data) {
		this.cache = new Cache(data.cache.cacheSize, data.cache.blockSize, data.cache.hitLatency, data.cache.missPenalty);
		for (int tag : data.cache.cacheBlockMap.keySet()) {
			Cache.CacheBlock block = data.cache.cacheBlockMap.get(tag);
			this.cache.cacheBlockMap.put(tag, new Cache.CacheBlock(block.data, block.cacheTag));
		}

		this.memory = new Memory(data.memory.size);
		if (data.memory.size >= 0) System.arraycopy(data.memory.data, 0, this.memory.data, 0, data.memory.size);
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
				cache.cacheBlockMap.remove(lruBlockIndex);
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

	public List<MemoryEntry> getMemoryEntries() {
		List<MemoryEntry> entries = new ArrayList<>();
		for (int i = 0; i < memory.size; i++) {
			entries.add(new MemoryEntry(i, memory.data[i]));
		}
		return entries;
	}

	public List<CacheEntry> getCacheEntries() {
		List<CacheEntry> entries = new ArrayList<>();
		for (Map.Entry<Integer, Cache.CacheBlock> entry : cache.cacheBlockMap.entrySet()) {
			entries.add(new CacheEntry(entry.getKey(), entry.getValue().cacheTag, entry.getValue().data));
		}
		return entries;
	}

	public String toString() {
		return "Cache:\n" + cache + "\nMemory:\n" + memory;
	}

	public String[] getMemory() {
		return memory.data;
	}

	private static class Cache {
		private final int blockSize;
		private final int cacheSize;
		private final int numBlocks;
		private final Map<Integer, CacheBlock> cacheBlockMap; // Maps block index to cache block
		private final LinkedList<Integer> usageOrder; // Keeps track of usage order
		private final int hitLatency;
		private final int missPenalty;

		public Cache(int cacheSize, int blockSize, int hitLatency, int missPenalty) {
			this.cacheSize = cacheSize;
			this.blockSize = blockSize;
			this.numBlocks = cacheSize / blockSize;
			this.cacheBlockMap = new HashMap<>();
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
			return cacheBlockMap.containsKey(blockIndex) && cacheBlockMap.get(blockIndex).cacheTag == tag;
		}

		public boolean isCacheFull() {
			return cacheBlockMap.size() == numBlocks;
		}

		// Get data using address from cache
		public String getData(int address) {
			int blockIndex = (address / blockSize) % numBlocks;
			// Move accessed block to the end of the usage list
			usageOrder.remove((Integer) blockIndex);
			usageOrder.addLast(blockIndex);

			return cacheBlockMap.get(blockIndex).data;
		}

		// Write data to cache using address
		public void writeData(int address, String data) {
			int blockIndex = (address / blockSize) % numBlocks;
			int tag = address / blockSize;

			if (isCacheFull() && !cacheBlockMap.containsKey(blockIndex)) {
				// Evict the least recently used block
				int lruBlockIndex = usageOrder.removeFirst();
				cacheBlockMap.remove(lruBlockIndex);
			}

			// Add new block to cache and update usage order
			cacheBlockMap.put(blockIndex, new CacheBlock(data, tag));
			usageOrder.remove((Integer) blockIndex);
			usageOrder.addLast(blockIndex);
		}

		public String toString() {
			// return cache as such [index]: tag, data
			StringBuilder sb = new StringBuilder();
			for (Map.Entry<Integer, CacheBlock> entry : cacheBlockMap.entrySet()) {
				if (entry.getValue() != null)
					sb.append("[").append(entry.getKey()).append("]: ").append(entry.getValue().cacheTag).append(", ").append(entry.getValue().data).append("\n");
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

	private static class Memory {
		private final int size;
		private final String[] data;

		public Memory(int size) {
			this.size = size;
			this.data = new String[size];
			Random random = new Random();
			for (int i = 0; i < size; i++) {
				data[i] = Double.toString(random.nextDouble(100));
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
				sb.append("[").append(i).append("]: ").append(data[i]).append("\n");
			}
			return sb.toString();
		}
	}

	public static class CacheEntry {
		int index;
		int tag;
		String data;

		public CacheEntry(int index, int tag, String data) {
			this.index = index;
			this.tag = tag;
			this.data = data;
		}

		public int getIndex() {
			return index;
		}

		public int getTag() {
			return tag;
		}

		public String getData() {
			return data;
		}

		public String toString() {
			return index + ": " + tag + ", " + data;
		}
	}

	public static class MemoryEntry {
		int index;
		String value;

		public MemoryEntry(int index, String value) {
			this.index = index;
			this.value = value;
		}

		public int getIndex() {
			return index;
		}

		public String getValue() {
			return value;
		}

		public String toString() {
			return index + ": " + value;
		}
	}
}