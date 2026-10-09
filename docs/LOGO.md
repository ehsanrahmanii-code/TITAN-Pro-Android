# TITAN Pro Logo

## Design

- Dark navy base (`#050816`)
- Neon gradient: cyan `#22D3EE` → violet `#A78BFA` → pink `#F472B6`
- Geometric letter **T** with soft glow
- Hexagon + orbital ring (analysis / multi-timeframe metaphor)
- Small **PRO** pill badge

## Files

| Path | Use |
|------|-----|
| `assets/logo/titan_logo.svg` | Marketing / README / full logo |
| `assets/logo/titan_icon_fg.svg` | Adaptive icon foreground |
| `app/src/main/res/drawable/ic_titan_logo.xml` | Vector drawable in-app |
| `app/src/main/res/drawable/ic_titan_background.xml` | Adaptive icon background |

## Export PNG (optional)

```bash
# with inkscape or rsvg-convert
rsvg-convert -w 512 -h 512 assets/logo/titan_logo.svg -o assets/logo/titan_512.png
```
