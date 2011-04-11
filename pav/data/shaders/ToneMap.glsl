
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

uniform sampler2D image_tex; 
uniform sampler2D bloom_tex;

// Control exposure with this value.
uniform float exposure;
// How much bloom to add.
uniform float bloom;
// Max bright.
uniform float bright;

void main()
{
    vec2 st = gl_TexCoord[0].st;
    vec4 color = texture2D(image_tex, st);
    vec4 colorBloom = texture2D(bloom_tex, st);

    // Add bloom to the image
    color += colorBloom * bloom;

    // Perform tone-mapping.
    float Y = dot(vec4(0.30, 0.59, 0.11, 0.0), color);
    float YD = exposure * (exposure / bright + 1.0) / (exposure + 1.0);
    color *= YD;
	
    gl_FragColor = color;
}
