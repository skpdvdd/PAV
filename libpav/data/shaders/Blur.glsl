
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

uniform sampler2D tex;
uniform vec2 offset;
uniform float DarkenFactor;

void main(void)
{
	float dx = offset.s;
	float dy = offset.t;
	vec2 st = gl_TexCoord[0].st;

	// Apply 3x3 gaussian filter
	vec4 color	 = 4.0 * texture2D(tex, st);
	color		+= 2.0 * texture2D(tex, st + vec2(+dx, 0.0));
	color		+= 2.0 * texture2D(tex, st + vec2(-dx, 0.0));
	color		+= 2.0 * texture2D(tex, st + vec2(0.0, +dy));
	color		+= 2.0 * texture2D(tex, st + vec2(0.0, -dy));
	color		+= texture2D(tex, st + vec2(+dx, +dy));
	color		+= texture2D(tex, st + vec2(-dx, +dy));
	color		+= texture2D(tex, st + vec2(-dx, -dy));
	color		+= texture2D(tex, st + vec2(+dx, -dy));
	
	gl_FragColor = color / 16.0 - DarkenFactor;
}
