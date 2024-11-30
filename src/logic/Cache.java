package logic;

import java.util.HashMap;
import java.util.Map;

public class Cache {
	private final int blockSize;
	private final int cacheSize;
	private final int numBlocks;
	private final Map<Integer, CacheBlock> cache; // Maps block index to cache block
	private int hitLatency;
	private int missPenalty;

	public Cache(int cacheSize, int blockSize, int hitLatency, int missPenalty) {
		this.cacheSize = cacheSize;
		this.blockSize = blockSize;
		this.numBlocks = cacheSize / blockSize;
		this.cache = new HashMap<>();
		this.hitLatency = hitLatency;
		this.missPenalty = missPenalty;
	}

	public int access(int address) {
		int blockIndex = (address / blockSize) % numBlocks;
		int tag = address / blockSize;

		// Check for hit
		if (cache.containsKey(blockIndex) && cache.get(blockIndex).cacheTag == tag) {
			return hitLatency; // Cache hit
		}

		// Cache miss: Load the block
		cache.put(blockIndex, new CacheBlock(tag));
		return missPenalty; // Cache miss
	}

	// Represents a cache block
	private static class CacheBlock {
		int cacheTag;

		CacheBlock(int cacheTag) {
			this.cacheTag = cacheTag;
		}
	}
}
