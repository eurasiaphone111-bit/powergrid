package net.powergrid.blockentity;

/** Генератор энергии в сети. */
public interface EnergySource {
	/** Текущая выдаваемая мощность, EU/t. */
	int getOutput();
}
