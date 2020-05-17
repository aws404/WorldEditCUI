package com.mumfrey.worldeditcui.render.shapes;

import com.mumfrey.worldeditcui.render.RenderStyle;
import com.mumfrey.worldeditcui.render.LineStyle;
import com.mumfrey.worldeditcui.util.Vector3;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;

/**
 * Draws a polygon
 * 
 * @author yetanotherx
 * @author lahwran
 * @author Adam Mummery-Smith
 */
public class Render3DPolygon extends RenderRegion {
	private Vector3[] vertices;

	public Render3DPolygon(RenderStyle style, Vector3... vertices) {
		super(style);
		this.vertices = vertices;
	}

	@Override
	public void render(Vector3 cameraPos) {
		System.out.println("render Render3DPolygon");
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buf = tessellator.getBuffer();

		for (LineStyle line : this.style.getLines()) {
			if (!line.prepare(this.style.getRenderType())) {
				continue;
			}

			buf.begin(0x2, VertexFormats.POSITION);
			line.applyColour();
			for (Vector3 vertex : this.vertices) {
				// buf.vertex(vertex.getX() - cameraPos.getX(), vertex.getY() -
				// cameraPos.getY(), vertex.getZ() - cameraPos.getZ()).next();
				buf.vertex(vertex.getX(), vertex.getY(), vertex.getZ()).next();
			}
			tessellator.draw();
		}
	}
}
