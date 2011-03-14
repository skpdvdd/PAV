
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

uniform float T1;
uniform float T2;
uniform float Intensity;
uniform vec4 LumaCoeffs;
uniform sampler2D tex;

void main(void)
{
    vec2 st = gl_TexCoord[0].st;
    vec4 color = texture2D(tex, st);

	float luminance = dot(color, LumaCoeffs);
	
	if(luminance > T1) {
		float k = smoothstep(T1, T2, luminance);
		gl_FragColor = color * (1.0 + k * Intensity);
	}
	else {
		gl_FragColor = color;
	}
}