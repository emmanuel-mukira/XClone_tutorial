# Workflow Prompt for Code Explanation

Use this prompt structure when you want detailed code explanations for exam preparation.

## Workflow Name
`/write-code-explanation-learning`

## Prompt Template

```
@[/write-code-explanation-learning] explain [file1] [file2] [file3]...
```

## What It Should Do

Create or update an `explainer.md` file that:

1. **Breaks down code line-by-line** with syntax explanations
2. **Explains the purpose** of each component in the overall system
3. **Provides exam-ready knowledge** - understanding + syntax
4. **Follows a consistent structure:**
   - File purpose
   - Important components/APIs
   - Full code or key snippets
   - Line-by-line breakdown
   - Key concepts for exams
   - Exam tips

## Example Usage

```
@[/write-code-explanation-learning] explain @[MainActivity.kt] @[HomeScreen.kt] @[TweetCard.kt]
```

## Expected Output Structure

### For Each File:

```markdown
## FileName.kt — Brief Description

### Purpose
What this file does and why it exists

### Important Components
- Component1: What it does
- Component2: What it does

### Full Code (or Key Sections)
```kotlin
// Actual code here
```

### Line-by-Line Breakdown

**1. First concept:**
```kotlin
code snippet
```
- Explanation point 1
- Explanation point 2
- Syntax note

**2. Second concept:**
```kotlin
code snippet
```
- Explanation
- Why this pattern

### Key Concepts for Exams

**Pattern name:**
```kotlin
example code
```
- What to remember
- Common mistakes

**Exam tip:** Specific advice for exams
```

## Key Requirements

1. **Explain syntax clearly** - Don't assume knowledge
   - Example: `by` delegate, `?.let`, `?:` Elvis operator

2. **Show code snippets** - Not just descriptions
   - Use actual code from the files
   - Highlight key patterns

3. **Exam focus** - Help student reproduce code
   - What to memorize
   - Common patterns
   - Syntax shortcuts

4. **Consistent formatting:**
   - Use `###` for sections
   - Use `**Bold:**` for concepts
   - Use code blocks with `kotlin` language
   - Add "Exam tip:" callouts

5. **Progressive detail:**
   - Start with overview
   - Then detailed breakdown
   - End with key takeaways

## What Makes It Different from Regular Explanations

❌ **Regular explanation:**
"This code creates a drawer with navigation items."

✅ **Exam-ready explanation:**
```markdown
**Drawer structure:**
```kotlin
ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = { ... }
)
```
- `ModalNavigationDrawer`: Material 3 component for side drawer
- `drawerState`: State holder (Open/Closed) created with `rememberDrawerState()`
- `drawerContent`: Lambda defining what's inside the drawer
- Pattern to remember: `remember*` functions preserve state across recompositions

**Exam tip:** Always use `rememberDrawerState()` not just `DrawerState()`
```

## Integration with Existing Workflow

Add to `.windsurf/workflows/write-code-explanation-learning.md`:

```markdown
---
description: Explain code for exam preparation
---

When the user mentions this workflow:

1. Read the specified files
2. Create/update explainer.md with:
   - Line-by-line code breakdown
   - Syntax explanations (assume beginner knowledge)
   - Purpose in overall system
   - Exam-focused tips and patterns
3. Follow the structure in WORKFLOW_PROMPT.md
4. Focus on understanding + reproducibility
5. Use code snippets, not just prose
6. Add "Exam tip:" callouts
7. Explain Kotlin syntax (by, ?., ?:, etc.)

Goal: Student can read this and write similar code in an exam without reference.
```

## Example Workflow File Content

```markdown
---
description: Explain code for exam preparation with syntax breakdown
---

# Code Explanation for Learning

When user requests code explanation:

1. **Read specified files** completely
2. **Create detailed explainer** with:
   - File purpose and role in app
   - Important components/APIs used
   - Full code or key sections
   - Line-by-line breakdown with syntax
   - Exam-focused key concepts
   - Practical exam tips

3. **Format requirements:**
   - Use ### for sections
   - Bold important terms
   - Code blocks with kotlin language
   - Explain ALL syntax (by, ?., ?:, etc.)
   - Add "Exam tip:" callouts

4. **Focus on:**
   - Understanding (why this code)
   - Syntax (how to write it)
   - Patterns (what to remember)
   - Reproducibility (can write in exam)

5. **Output to:** explainer.md or explainer2.md (numbered if multiple)

**Goal:** Student reads this and can write similar code from memory in an exam.
```

## Testing the Workflow

Try these commands:

```
@[/write-code-explanation-learning] explain @[MainActivity.kt]
@[/write-code-explanation-learning] explain @[AuthRepository.kt] @[SignUpScreen.kt]
@[/write-code-explanation-learning] explain the Firebase integration code
```

Should produce detailed, exam-ready explanations with:
- ✅ Syntax breakdowns
- ✅ Code snippets
- ✅ Exam tips
- ✅ Pattern recognition
- ✅ Reproducible knowledge
