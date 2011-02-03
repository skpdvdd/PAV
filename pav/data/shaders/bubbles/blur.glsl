
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

uniform sampler2D Tex;
uniform sampler2D Age;
uniform vec2 Offset;
uniform float DarkenFactor;

void main(void)
{
	float dx = Offset.s;
	float dy = Offset.t;
	vec2 st = gl_TexCoord[0].st;

	vec4 color = texture2D(Tex, st);
	float alpha = color.a;
	
	color += texture2D(Tex, st + vec2(+dx, 0.0));
	color += texture2D(Tex, st + vec2(-dx, 0.0));
	color += texture2D(Tex, st + vec2(0.0, +dy));
	color += texture2D(Tex, st + vec2(0.0, -dy));
	color += texture2D(Tex, st + vec2(+dx, +dy));
	color += texture2D(Tex, st + vec2(-dx, +dy));
	color += texture2D(Tex, st + vec2(-dx, -dy));
	color += texture2D(Tex, st + vec2(+dx, -dy));
	color = color / 9.0;
	
	float age = texture2D(Age, st).r;

	if(age == 1.0) {
		alpha = color.a;
	}
	
	gl_FragColor = vec4(vec3(color), alpha - DarkenFactor);
}