
/*
 * Processing Audio Visualization (PAV)
 * Copyright (C) 2011  Christopher Pramerdorfer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

uniform float EmphBase;
uniform float EmphBlend;
uniform sampler2D Base;
uniform sampler2D Blend;

void main(void)
{
    vec2 st = gl_TexCoord[0].st;
    vec4 base = texture2D(Base, st);
    vec4 blend = texture2D(Blend, st);
    vec4 mix = base;
    
    if(blend.a > 0.0) {
    	float cBase = base.a + EmphBase;
    	float cBlend = 1.0 + EmphBlend - cBase;
    	
    	mix = vec4(base.rgb * cBase + blend.rgb * cBlend, base.a + blend.a);
    }

	gl_FragColor = mix;
}