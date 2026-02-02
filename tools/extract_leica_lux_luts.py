#!/usr/bin/env python3
"""
Extract and convert Leica Camera LUTs from .data files to CUBE format.

The Leica .data files contain 64x64x64 3D LUTs stored as RGBA float16 values.
Each entry is 16 bytes containing 2 consecutive RGBA pixels (8 float16 values).
"""

import struct
import os
import argparse
from pathlib import Path


def float16_to_float32(h16):
    """Convert half-precision float (uint16) to float32."""
    sign = (h16 >> 15) & 1
    exp = (h16 >> 10) & 0x1f
    mant = h16 & 0x3ff

    if exp == 0:
        if mant == 0:
            return -0.0 if sign else 0.0
        else:
            # Subnormal number
            f = (mant / 1024.0) * (2 ** -14)
            return -f if sign else f
    elif exp == 31:
        # Infinity or NaN
        return float('-inf') if sign else float('inf')
    else:
        f = (1 + mant / 1024.0) * (2 ** (exp - 15))
        return -f if sign else f


def read_leica_lut(filepath):
    """Read a Leica .data LUT file and return RGB values as a 3D array."""
    with open(filepath, 'rb') as f:
        data = f.read()

    # Each entry is 16 bytes = 8 float16 values
    # File contains 131072 entries = 262144 RGBA pixels = 64^3
    lut_size = 64
    total_pixels = lut_size ** 3  # 262144

    rgba_values = []
    for i in range(total_pixels):
        # Each pixel uses 8 bytes (4 float16 values)
        offset = i * 8
        if offset + 8 > len(data):
            break

        # Read 4 float16 values (RGBA)
        pixel_data = data[offset:offset + 8]
        r_h = struct.unpack('<H', pixel_data[0:2])[0]
        g_h = struct.unpack('<H', pixel_data[2:4])[0]
        b_h = struct.unpack('<H', pixel_data[4:6])[0]
        a_h = struct.unpack('<H', pixel_data[6:8])[0]

        r = float16_to_float32(r_h)
        g = float16_to_float32(g_h)
        b = float16_to_float32(b_h)
        a = float16_to_float32(a_h)

        rgba_values.append((r, g, b, a))

    return rgba_values, lut_size


def write_cube_file(output_path, rgba_values, lut_size, title):
    """Write RGB values to Adobe CUBE format."""
    with open(output_path, 'w') as f:
        f.write(f"TITLE {title}\n")
        f.write(f"# Generated from Leica Camera LUT\n")
        f.write(f"LUT_3D_SIZE {lut_size}\n")

        # Write data in domain 0-1
        for r, g, b, a in rgba_values:
            # Clamp values to 0-1 range and write
            r_clamped = max(0.0, min(1.0, r))
            g_clamped = max(0.0, min(1.0, g))
            b_clamped = max(0.0, min(1.0, b))
            f.write(f"{r_clamped:.6f} {g_clamped:.6f} {b_clamped:.6f}\n")


def main():
    parser = argparse.ArgumentParser(description='Convert Leica LUT .data files to CUBE format')
    parser.add_argument('input_dir', help='Directory containing .data files')
    parser.add_argument('output_dir', help='Output directory for CUBE files')
    args = parser.parse_args()

    input_dir = Path(args.input_dir)
    output_dir = Path(args.output_dir)
    output_dir.mkdir(exist_ok=True)

    # Find all .data files
    data_files = list(input_dir.glob('*.data'))

    print(f"Found {len(data_files)} LUT files")

    for data_file in data_files:
        print(f"Processing: {data_file.name}")

        try:
            rgba_values, lut_size = read_leica_lut(data_file)

            # Create output filename
            output_name = data_file.stem + '.cube'
            output_path = output_dir / output_name

            # Write CUBE file
            title = data_file.stem.replace('_', ' ')
            write_cube_file(output_path, rgba_values, lut_size, title)

            print(f"  -> {output_path}")

        except Exception as e:
            print(f"  ERROR: {e}")


if __name__ == '__main__':
    main()
