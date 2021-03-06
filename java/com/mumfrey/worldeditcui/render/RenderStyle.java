package com.mumfrey.worldeditcui.render;


import com.mumfrey.worldeditcui.config.Colour;

/**
 * Render style adapter, can be one of the built-in {@link ConfiguredColour}s
 * or a user-defined style from a custom payload
 * 
 * @author Adam Mummery-Smith
 */
public interface RenderStyle
{
	/**
	 * Rendering type for this line
	 */
	public enum RenderType
	{
		/**
		 * Render type to draw lines regardless of depth
		 */
		ANY(0x207),
		
		/**
		 * Render type for "hidden" lines (under world geometry)
		 */
		HIDDEN(0x206),
		
		/**
		 * Render type for visible lines (over world geometry) 
		 */
		VISIBLE(0x201);
		
		final int depthFunc;

		private RenderType(int depthFunc)
		{
			this.depthFunc = depthFunc;
		}
		
		public boolean matches(RenderType other)
		{
			return other == RenderType.ANY ? true : other == this;
		}
	}

	public abstract void setRenderType(RenderType renderType);
	
	public abstract RenderType getRenderType();
	
	public abstract void setColour(Colour colour);

	public abstract Colour getColour();
	
	public abstract LineStyle[] getLines();
}