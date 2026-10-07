package com.mrh0.createaddition.energy.network;

import java.util.Collections;
import java.util.Set;

import com.mrh0.createaddition.config.CACommonConfig;

public class EnergyNetwork {
	private int id;
	// Input
	private long inBuff;
	private long inDemand;
	// Output
	private long outBuff;
	private long outBuffRetained;
	private long outDemand;
	private boolean valid;

	private long pulled = 0;
	private long pushed = 0;

	Set<PortKey> members = Collections.emptySet();

	public EnergyNetwork() {
		this.inBuff = 0;
		this.outBuff = 0;
		this.outBuffRetained = 0;
		this.inDemand = 0;
		this.outDemand = 0;
		this.valid = true;
	}

	public long getMaxBuff() {
		return Math.min(members.size() * (outDemand + inDemand * 2 + 10), CACommonConfig.COMMON.CONNECTOR_NETWORK_INTERNAL_BUFFER.get());
	}

	public void tick(int index) {
		this.id = index;
		long t = outBuff;
		outBuff = inBuff;
		outBuffRetained = outBuff;
		inBuff = t;
		outDemand = inDemand;
		inDemand = 0;

		pulled = 0;
		pushed = 0;
	}

	public long getBuff() {
		return outBuffRetained;
	}

	// Returns the amount of energy pushed to network
	public long push(long energy, boolean simulate) {
		energy = Math.min(getMaxBuff() - inBuff, energy);
		energy = Math.max(energy, 0);
		if (!simulate) {
			inBuff += energy;
			pushed += energy;
		}
		return energy;
	}

	public long push(long energy) {
		return push(energy, false);
	}

	public long demand(long demand) {
		this.inDemand += demand;
		return demand;
	}

	public long getDemand() {
		return outDemand;
	}

	public long getPulled() {
		return pulled;
	}

	public long getPushed() {
		return pushed;
	}

	// Returns amount of energy pulled from network
	public long pull(long energy, boolean simulate) {
		long r = Math.max(Math.min(energy, outBuff), 0);
		if (!simulate) {
			outBuff -= r;
			pulled += r;
		}
		return r;
	}

	public long pull(long max) {
		return pull(max, false);
	}

	void absorb(EnergyNetwork other) {
		restore(other.inBuff, other.outBuff);
		other.drain();
	}

	void drain() {
		inBuff = 0;
		outBuff = 0;
		outBuffRetained = 0;
	}

	void restore(long in, long out) {
		inBuff += in;
		outBuff += out;
		outBuffRetained = outBuff;
	}

	boolean hasStoredEnergy() {
		return inBuff > 0 || outBuff > 0;
	}

	long getStoredIn() {
		return inBuff;
	}

	long getStoredOut() {
		return outBuff;
	}

	public void invalidate() {
		this.valid = false;
	}

	public boolean isValid() {
		return this.valid;
	}

	public void removed() {}

	public int getId() {
		return id;
	}
}
