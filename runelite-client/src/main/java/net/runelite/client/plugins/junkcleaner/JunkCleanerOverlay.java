/*
 * Copyright (c) 2019 Andrew <https://github.com/andrewghaly>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.runelite.client.plugins.junkcleaner;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import net.runelite.api.Query;
import net.runelite.api.queries.BankItemQuery;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.util.QueryRunner;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;

public class JunkCleanerOverlay extends Overlay {
	private static final float HIGH_ALCHEMY_CONSTANT = 0.6f;

	private final QueryRunner queryRunner;
	private final ItemManager itemManager;
	private final JunkCleanerPlugin plugin;
	private final JunkCleanerConfig config;
	private final Client client;

	@Inject
	private JunkCleanerOverlay(QueryRunner queryRunner, ItemManager itemManager, JunkCleanerPlugin plugin,
			JunkCleanerConfig config, Client client) {
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.LOW);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		this.queryRunner = queryRunner;
		this.itemManager = itemManager;
		this.plugin = plugin;

		this.config = config;
		this.client = client;
	}

	@Override
	public Dimension render(Graphics2D graphics) {
		// Now query the inventory for the tagged item ids
		final Query query = new BankItemQuery();
		final WidgetItem[] widgetItems = queryRunner.runQuery(query);

		// Iterate through all found items and draw the outlines
		for (final WidgetItem item : widgetItems) {

			final Color color = new Color(152, 0, 13, 50);
			if (color != null && isLessThanValue(item.getId(), item.getQuantity())) {
				final BufferedImage outline = itemManager.getItemOutline(item.getId(), item.getQuantity(), color);
				graphics.setPaint(color);
				graphics.fillRect(item.getCanvasLocation().getX() + 1, item.getCanvasLocation().getY() + 1,
						outline.getWidth(), outline.getHeight());
			}
		}
		return null;
	}

	boolean isLessThanValue(int itemId, int itemQuantity) {
		final ItemComposition itemComposition = itemManager.getItemComposition(itemId);

		int price = itemComposition.getPrice();

		long highAlchPrice = (long) Math.round(price * HIGH_ALCHEMY_CONSTANT) * (long) itemQuantity;
		int gePrice = itemManager.getItemPrice(itemId);
		if (gePrice < 1 || gePrice * itemQuantity > config.minGpValue()) {
			return false;
		}

		// if (price > 0 && highAlchPrice > config.minGpValue()) {
		// return false;
		// }
		return true;
	}
}