# AI-Assisted Development Guide

## Phase-Based Workflow

This project uses a simple phase-based approach to AI-assisted development.

### Phases

1. **Analysis** - Document differences between source (React) and target (Android)
2. **Design System** - Create matching UI components, colors, typography
3. **Implementation** - Refactor Android code to match design
4. **Testing** - Side-by-side comparison, bug reporting

### Rate Limit Handling

- **Claude**: Primary agent for design/implementation
- **Backup agents**: OpenCode (me) or Gemini
- **Fallback**: When Claude is rate-limited, backup agent continues work tagged as `/* PENDING CLAUDE REVIEW */`
- **User review**: You review before merging backup agent work

### Rolling Builds

- Each phase creates a working checkpoint
- Test incrementally after each phase
- No "big bang" deployments

---

## Version Control Rules

### Before Major Work
```bash
git add .
git commit -m "before [phase-name]"
```

### Branch by Phase
```bash
git checkout -b phase-1-analysis
git checkout -b phase-2-design-system
git checkout -b phase-3-implementation
```

### Attribution
Each commit should include agent attribution:
```bash
git commit -m "ClockDisplay brutalist styling (Claude)"
git commit -m "Build configuration fix (OpenCode)"
git commit -m "Animation testing (Gemini)"
```

---

## Prompting Guidelines

### DO
- **Give context files**: Reference React components by path
- **Show examples**: "Like line 40 in ClockDisplay.tsx"
- **Be prescriptive**: "Use Framer Motion-like animations in Compose"
- **Set constraints**: "Keep under 500 lines per file"
- **Specific location**: "Fix app/src/main/java/com/chronos/alarm/ui/components/ClockDisplay.kt"

### DON'T
- **Vague requests**: "Make it better" or "Fix the UI"
- **Ask for opinions**: "What do you think?" → Ask for analysis
- **Skip context**: Assume agent knows everything

---

## Testing Guidelines

### Incremental Testing
- Test after each commit
- Install APK to device
- Side-by-side comparison

### Screenshots
- Take photos of Android app after every UI change
- Store in `screenshots/` folder with timestamps
- Name format: `YYYY-MM-DD_feature_name.png`

### Comparison
- React app open in browser (localhost:3000)
- Android app on connected device
- Verify each screen matches

---

## Agent Roles

### OpenCode (Primary)
- Analysis and documentation
- Build configuration and infrastructure
- Rate limit fallback for design/implementation
- Tags work: `/* PENDING CLAUDE REVIEW */`

### Claude (Design & Implementation)
- Design system creation
- Android Compose refactoring
- Component implementation
- Main build artifact

### Gemini (Testing & QA)
- Side-by-side screenshot comparison
- Interaction testing on device
- Bug reporting and verification
- Test documentation

---

## Documentation Requirements

### Agent Outputs
Create `agent-outputs/` folder:
```
agent-outputs/
  ├── phase-1-analysis.md
  ├── phase-2-design-system.md
  ├── phase-3-implementation.md
  └── phase-4-testing.md
```

### Changelog
Track all agent contributions in `CHANGELOG.md`:
```markdown
## [Phase] - YYYY-MM-DD

### Changes
- [Agent Name] Description of change
- [Agent Name] Description of change

### Files Modified
- path/to/file
- path/to/file
```

---

## Communication Guidelines

### For Agents (Read This!)

1. **Be explicit**: Never assume context
2. **Reference files**: Always include file paths
3. **Tag conflicts**: When disagreeing with previous agent work, add `/* CONFLICT REVIEW NEEDED */`
4. **No ego**: You're a tool, not an architect
5. **Simplicity wins**: 90% match to source = success
6. **Document decisions**: Why did you choose X over Y?

### For Human (You)

1. **Review before merging**: Especially backup agent work
2. **Test often**: Don't wait for "completion"
3. **Take screenshots**: Visual feedback is everything
4. **Git is safety**: Commit before each phase
5. **Adjust phases**: This guide is flexible

---

## Emergency Protocols

### If Build Fails
1. Check recent commits
2. Revert to last working: `git reset --hard HEAD~1`
3. Document failure in `agent-outputs/failures.md`

### If Agent Goes Rogue
1. Stop immediately
2. Don't delete code (might be useful)
3. Branch out: `git checkout -b experiment/failed-agent-work`
4. Continue from last good commit

### If Rate Limits Hit
1. Check which agent is available
2. Switch to backup agent
3. Tag work for review
4. Resume when original agent is back

---

## Tools and Commands

### Android Development
```bash
# Build APK
podman build -f Dockerfile.build -t chronos-builder .
podman run --rm -v "$(pwd)":/home/circleci/project:z -w /home/circleci/project docker.io/cimg/android:2024.10.1 ./gradlew assembleDebug --no-daemon

# Install to device
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch app
adb shell am start -n com.chronos.alarm/.MainActivity
```

### React Development
```bash
cd /tmp/luch4ik-chronos
npm install
npm run dev
# Available at http://localhost:3000
```

---

## Success Criteria

### Phase 1 (Analysis)
- [ ] Document all React components
- [ ] List all differences with Android
- [ ] Prioritize fixes by impact

### Phase 2 (Design System)
- [ ] Color scheme matches React
- [ ] Typography matches React
- [ ] Brutalist components implemented
- [ ] Animation system defined

### Phase 3 (Implementation)
- [ ] All screens refactored
- [ ] Animations working
- [ ] APK builds successfully
- [ ] No lint errors blocking

### Phase 4 (Testing)
- [ ] Screenshot comparison complete
- [ ] All interactions tested
- [ ] Bug report created (if any)
- [ ] Ready for user review

---

## Golden Rule

**Simplicity over cleverness. Humans refine, AI scaffolds.**

If you're stuck, go back to the previous working state and try a simpler approach. No complex orchestrations, no hidden workflows, no AI decision-making.

---

*Last updated: 2026-01-01*
